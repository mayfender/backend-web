package com.may.ple.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.may.ple.backend.bussiness.jasper.JasperReportEngine;
import com.may.ple.backend.criteria.OrderCriteriaReq;
import com.may.ple.backend.entity.OrderGroup;
import com.may.ple.backend.entity.Receiver;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.OrderNumberUtil;
import com.may.ple.backend.utils.ZipUtil;

@Service
public class OrderGroupService {
	private static final Logger LOG = Logger.getLogger(OrderGroupService.class.getName());
	private OrderService orderService;
	private DbFactory dbFactory;
	@Value("${file.path.base}")
	private String basePath;

	@Autowired
	public OrderGroupService(DbFactory dbFactory, OrderService orderService) {
		this.dbFactory = dbFactory;
		this.orderService = orderService;
	}

	public void proceed(OrderCriteriaReq req) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			Query query = Query.query(Criteria.where("enabled").is(true));
			query.with(new Sort("order"));
			query.fields().include("id");
			Receiver firstRec = dealerTemp.findOne(query, Receiver.class);

			Date now = Calendar.getInstance().getTime();
			Map<String, Object> newfamilies, oldfamilies, recPrice, familyVal;
			List<Integer> typeLst;
			List<String> ordToReove;
			List<Map> sumOrderLst;
			OrderGroup ordGroup;
			Double myPrice, otherPrice;
			String type, orderNumber;
			String tab[] = OrderNumberUtil.tabIndex;

			for (int i = 0; i < tab.length; i++) {
				type = tab[i];
				typeLst = orderService.getGroup(type, false);
				sumOrderLst = orderService.getSumOrder(type, typeLst, null, req.getPeriodId(), null, null, req.getDealerId());

				query = Query.query(Criteria.where("periodId").is(new ObjectId(req.getPeriodId())).and("type").is(type));
				ordGroup = dealerTemp.findOne(query, OrderGroup.class);

				if(sumOrderLst.size() == 0 && (ordGroup == null || ordGroup.getFamilies().size() == 0)) {
					LOG.info("type: " + type + " has ignored");
					continue;
				}

				if(ordGroup == null) {
					ordGroup = new OrderGroup();
					ordGroup.setCreatedDateTime(now);
					ordGroup.setUpdatedDateTime(now);
					ordGroup.setPeriodId(new ObjectId(req.getPeriodId()));
					ordGroup.setType(type);
				} else {
					ordGroup.setUpdatedDateTime(now);
				}

				oldfamilies = ordGroup.getFamilies();
				newfamilies = new HashMap();
				ordToReove = new ArrayList<>();

				for (Map ordMap : sumOrderLst) {
					recPrice = new HashMap<>();
					recPrice.put(firstRec.getId(), ordMap.get("totalPrice"));
					orderNumber = ordMap.get("_id").toString();
					newfamilies.put(orderNumber, recPrice);

					if(oldfamilies != null) {
						if(!oldfamilies.containsKey(orderNumber)) {
							oldfamilies.put(orderNumber, recPrice);
						}
					}
				}

				if(oldfamilies != null) {
					for (Map.Entry<String, Object> fam : oldfamilies.entrySet()) {
						if(!newfamilies.containsKey(fam.getKey())) {
							ordToReove.add(fam.getKey());
							continue;
						}

						familyVal = (Map)fam.getValue();
						otherPrice = 0.0;

						for (Map.Entry<String, Object> item : familyVal.entrySet()) {
							if(!item.getKey().equals(firstRec.getId())) {
								otherPrice += (Double)item.getValue();
							}
						}

						myPrice = (Double)((Map)newfamilies.get(fam.getKey())).get(firstRec.getId());
						familyVal.put(firstRec.getId(), myPrice - otherPrice);
					}

					//--: Remove order number that's not the same.
					for (String ord : ordToReove) {
						oldfamilies.remove(ord);
					}

					newfamilies = oldfamilies;
				}

				ordGroup.setFamilies(newfamilies);
				dealerTemp.save(ordGroup);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map<String, Object> getData(OrderCriteriaReq req, boolean isGetOrderGroup) throws Exception {
		try {
			Map<String, Object> data = new HashMap<>();
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			Query query = Query.query(Criteria.where("periodId").is(new ObjectId(req.getPeriodId())).and("type").is(req.getTab()));
			OrderGroup orderGroup = dealerTemp.findOne(query, OrderGroup.class);
			if(orderGroup == null) return data;

			Map<String, Object> families = orderGroup.getFamilies();
			if(families == null) return data;

			if(isGetOrderGroup) {
				data.put("orderGroup", orderGroup);
			}

			Map<String, Object> todDataMap = new HashMap<>();
			Map<String, Object> chkTodOrderMap;
			List<String> receiverIds = req.getReceiverIds();
			Map<String, Object> familyVal, ordMap, innerData;
			List<Map> orderList;

			for (Map.Entry<String, Object> fam : families.entrySet()) {

				familyVal = (Map)fam.getValue();
				for (Map.Entry<String, Object> item : familyVal.entrySet()) {

					for (String recId : receiverIds) {
						if(item.getKey().equals(recId)) {
							ordMap = new HashMap<>();
							ordMap.put("orderNumber", fam.getKey());
							ordMap.put("price", item.getValue());

							if(req.getTab().equals("51")) {
								calTotalTodPrice(todDataMap, recId, fam.getKey(), (Double)item.getValue());
							}

							if(data.containsKey(recId)) {
								innerData = (Map)data.get(recId);
								orderList = (List<Map>)innerData.get("orderList");
								innerData.put("sumPrice", (Double)innerData.get("sumPrice") + (Double)ordMap.get("price"));

								orderList.add(ordMap);
							} else {
								orderList = new ArrayList<>();
								orderList.add(ordMap);

								innerData = new HashMap<>();
								innerData.put("orderList", orderList);
								innerData.put("sumPrice", ordMap.get("price"));

								data.put(recId, innerData);
							}
						}
					}
				}
			}

			if(req.getTab().equals("51")) {
				for (String recId : receiverIds) {
					if(todDataMap.containsKey(recId) && data.containsKey(recId)) {
						chkTodOrderMap = (Map)todDataMap.get(recId);
						((Map)data.get(recId)).put("sumPrice", chkTodOrderMap.get("todPrice"));
					}
				}
			}

			for (String recId : receiverIds) {
				if(data.containsKey(recId)) {
					orderList = (List<Map>)((Map)data.get(recId)).get("orderList");
					listMapPriceSortingDesc(orderList);
				}
			}

			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void moveByPrice(OrderCriteriaReq req) throws Exception {
		try {
			List<String> recIdLst = new ArrayList<>();
			recIdLst.add(req.getMoveFromId());
			recIdLst.add(req.getMoveToId());

			req.setReceiverIds(recIdLst);
			Map<String, Object> data = getData(req, true);

			Map moveFromData = (Map)data.get(req.getMoveFromId());
			if(moveFromData == null) return;

			List<Map> orderFromList = (List<Map>)moveFromData.get("orderList");

			OrderGroup orderGroup = (OrderGroup)data.get("orderGroup");
			Map families = orderGroup.getFamilies();

			Double movedPrice, moveFromPrice;
			Map orderNumberMap;

			if(req.getOperator().equals("2")) {
				for (Map map : orderFromList) {
					moveFromPrice = (Double)map.get("price");

					if(moveFromPrice <= req.getPrice()) continue;

					movedPrice = moveFromPrice - req.getPrice();
					orderNumberMap = (Map)families.get(map.get("orderNumber"));

					//--
					orderNumberMap.put(req.getMoveFromId(), req.getPrice());

					if(orderNumberMap.containsKey(req.getMoveToId())) {
						orderNumberMap.put(req.getMoveToId(), (Double)orderNumberMap.get(req.getMoveToId()) + movedPrice);
					} else {
						orderNumberMap.put(req.getMoveToId(), movedPrice);
					}
				}
			} else {
				for (Map map : orderFromList) {
					orderNumberMap = (Map)families.get(map.get("orderNumber"));

					if(orderNumberMap.containsKey(req.getMoveToId())) {
						orderNumberMap.put(req.getMoveToId(), (Double)orderNumberMap.get(req.getMoveFromId()) + (Double)orderNumberMap.get(req.getMoveToId()));
					} else {
						orderNumberMap.put(req.getMoveToId(), (Double)orderNumberMap.get(req.getMoveFromId()));
					}
					orderNumberMap.remove(req.getMoveFromId());
				}
			}

			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			dealerTemp.save(orderGroup);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public byte[] exportData(OrderCriteriaReq req, Receiver receiver) throws Exception {
		try {
			LOG.info("Start exportData");
			List<String> receiverIds = new ArrayList<>();
			receiverIds.add(req.getReceiverId());
			req.setReceiverIds(receiverIds);

			Map<String, Object> chkTodOrderMap = new HashMap<>();
			String tabs[] = OrderNumberUtil.tabIndex4Export;
			String orderNumer, tab, label, sortingField;
			Map<String, Object> mapData, innerData;
			List<Map> dataOnTLLst, orderList;
			List<String> orderNumProb;
			boolean isTodContain;
			Double price;

			List<Map> ordFormatedLst3 = new ArrayList<>();
			List<Map> ordFormatedLst2Bon = new ArrayList<>();
			List<Map> ordFormatedLst2Lang = new ArrayList<>();
			List<Map> ordFormatedLstLoy = new ArrayList<>();
			List<Map> ordFormatedLstPare4 = new ArrayList<>();
			List<Map> ordFormatedLstPare5 = new ArrayList<>();
			List<Map> ordFormatedLstRunBon = new ArrayList<>();
			List<Map> ordFormatedLstRunLang = new ArrayList<>();
			List<Map> ordFormatedLstTod = new ArrayList<>();

			for (int i = 0; i < tabs.length; i++) {
				tab = tabs[i];

				req.setTab(tab);
				mapData = getData(req, false);
				innerData = (Map)mapData.get(req.getReceiverId());
				if(innerData == null) continue;

				//-------------: Get order to generate virtual order number.
				LOG.info("Start call getDataOnTL tab " + tab);
				sortingField = tab.equals("51") ? "todPrice" : "price";
				dataOnTLLst = (List<Map>)orderService.getDataOnTL(
						req.getPeriodId(), null, null,
						orderService.getGroup(tab, false),
						null, new Sort(Direction.DESC, sortingField),
						req.getDealerId(), null, null
				).get("orderLst");
				LOG.info("End call getDataOnTL tab " +tab);
				//-------------: Get order to generate virtual order number.

				LOG.info("Start prepare data to generate file tab " + tab);
				orderList = (List<Map>)innerData.get("orderList");
				for (Map ordMap : orderList) {
					orderNumer = ordMap.get("orderNumber").toString();
					price = (Double)ordMap.get("price");

					if(tab.equals("51")) {
						orderNumProb = OrderNumberUtil.getOrderNumProb(orderNumer);
						isTodContain = false;
						for (String todOrd : orderNumProb) {
							if(chkTodOrderMap.containsKey(todOrd)) {
								isTodContain = true;
								break;
							}
						}

						if(!isTodContain) {
							chkTodOrderMap.put(orderNumer, price);
							ordFormatedLstTod.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, "เฉพาะโต๊ด", tab, null));
						}
					} else if(tab.equals("1")) {
						ordFormatedLst3.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, null, tab, chkTodOrderMap));
					} else if(tab.equals("2")) {
						ordFormatedLst2Bon.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, null, tab, null));
					} else if(tab.equals("3")) {
						ordFormatedLst2Lang.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, null, tab, null));
					} else if(tab.equals("4") || tab.equals("41") || tab.equals("42") || tab.equals("43") || tab.equals("44")) {
						if(tab.equals("4")) {
							label = "ลอย ";
							ordFormatedLstLoy.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, label, tab, null));
						} else if(tab.equals("41")) {
							label = "แพ ";
							ordFormatedLstPare4.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, label, tab, null));
						} else if(tab.equals("42")) {
							label = "แพ ";
							ordFormatedLstPare5.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, label, tab, null));
						} else if(tab.equals("43")) {
							label = "วิ่งบน ";
							ordFormatedLstRunBon.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, label, tab, null));
						} else if(tab.equals("44")) {
							label = "วิ่งล่าง ";
							ordFormatedLstRunLang.addAll(makeVirtualPrice(dataOnTLLst, price, orderNumer, label, tab, null));
						} else {
							label = "";
						}
					}
				}
				LOG.info("End prepare data to generate file tab " + tab);
			}

			//---------: Remove TOD that is moved to 3 BON.
			LOG.debug("Call removeTod");
			removeTod(chkTodOrderMap, ordFormatedLstTod);

			double totalPrice = checkPrice(ordFormatedLst3, "### 3 TOD on 3 Bon", true);  //--: For TOD embedded.
			totalPrice += checkPrice(ordFormatedLstTod, "### Only 3 Tod", false);
			LOG.info("#### totalTodPrice " + String.format("%,.0f", totalPrice));

			totalPrice += checkPrice(ordFormatedLst3, "3 Bon", false);
			totalPrice += checkPrice(ordFormatedLst2Bon, "2 Bon", false);
			totalPrice += checkPrice(ordFormatedLst2Lang, "2 Lang", false);
			totalPrice += checkPrice(ordFormatedLstLoy, "1 Loy", false);
			totalPrice += checkPrice(ordFormatedLstPare4, "4 Pare", false);
			totalPrice += checkPrice(ordFormatedLstPare5, "5 Pare", false);
			totalPrice += checkPrice(ordFormatedLstRunBon, "Run Bon", false);
			totalPrice += checkPrice(ordFormatedLstRunLang, "Run Lang", false);
			LOG.info("totalPrice " + String.format("%,.0f", totalPrice));

			LOG.debug("Start Sorting");
			listMapPriceSortingDesc(ordFormatedLst2Bon);
			listMapPriceSortingDesc(ordFormatedLst2Lang);
			listMapPriceSortingDesc(ordFormatedLstLoy);
			listMapPriceSortingDesc(ordFormatedLstPare4);
			listMapPriceSortingDesc(ordFormatedLstPare5);
			listMapPriceSortingDesc(ordFormatedLstRunBon);
			listMapPriceSortingDesc(ordFormatedLstRunLang);
			LOG.debug("End Sorting");

			//-------: Include Pare4, Pare5, Run Bon, Run Lang to LOY.
			ordFormatedLstLoy.addAll(ordFormatedLstPare4);
			ordFormatedLstLoy.addAll(ordFormatedLstPare5);
			ordFormatedLstLoy.addAll(ordFormatedLstRunBon);
			ordFormatedLstLoy.addAll(ordFormatedLstRunLang);

			LOG.debug("Start Sorting");
			listMapPriceSortingDesc(ordFormatedLst3);
			listMapPriceSortingDesc(ordFormatedLstTod);
			LOG.debug("End Sorting");

			//-------: Include TOD to 3 Bon
			ordFormatedLst3.addAll(ordFormatedLstTod);

			//-------: Include LOY to 3 Bon
			ordFormatedLst3.addAll(ordFormatedLstLoy);

			return generateFile(req.getPeriodDate(), receiver,
							ordFormatedLst3,
							ordFormatedLst2Bon,
							ordFormatedLst2Lang
						);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private void removeTod(Map<String, Object> chkTodOrderMap, List<Map> ordFormatedLstTod) throws Exception {
		try {
			List<Map> removeItem = new ArrayList<>();
			boolean isContain;
			List<String> prob;
			String ordNumer;
			double todPrice;
			Map todMap;

			for (int i = 0; i < ordFormatedLstTod.size(); i++) {
				todMap = ordFormatedLstTod.get(i);
				ordNumer = todMap.get("orderNumer").toString();
				prob = OrderNumberUtil.getOrderNumProb(ordNumer);
				isContain = false;
				todPrice = 0.0;

				for (String ord : prob) {
					if(chkTodOrderMap.containsKey(ord)) {
						isContain = true;
						todPrice = (double)chkTodOrderMap.get(ord);
						break;
					}
				}

				if(isContain && todPrice == 0) {
					removeItem.add(todMap);
				}
			}

			ordFormatedLstTod.removeAll(removeItem);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private double checkPrice(List<Map> objList, String title, boolean isEmbTod) {
		double sumDummy = 0;
		String field = isEmbTod ? "todPrice" : "price";

		for (Map map : objList) {
			sumDummy += (Double)map.get(field);
		}
		LOG.info(title + "'s total " + field + " = " + String.format("%,.0f", sumDummy));
		return sumDummy;
	}

	private void listMapPriceSortingDesc(List<Map> ordList) {
		Collections.sort(ordList, new Comparator<Map>() {
			@Override
			public int compare(Map m1, Map m2) {
				int compare = ((Double)m1.get("price")).compareTo((Double)m2.get("price"));
				return compare == 1 ? -1 : (compare == 0 ? 0 : 1);
			};
		});
	}

	private List<Map> makeVirtualPrice(List<Map> dataOnTLLst, Double price, String orderNumer,
										String label, String tab, Map<String, Object> chkTodOrderMap) throws Exception {
		try {
			List<Map> ordListMap = new ArrayList<>();
			String ordFormated, orderNumerOnTl, todPriceFormated = "";
			double minimal = 30, todPrice = 0.0;
			Double priceOnTl;
			Map ordMap;
			label = label == null ? "" : label + " ";

			for (Map dataOnTLMap : dataOnTLLst) {
				if(price == 0) break;

				orderNumerOnTl = dataOnTLMap.get("orderNumber").toString();

				if(!orderNumer.equals(orderNumerOnTl)) continue;

				priceOnTl = tab.equals("51") ? (Double)dataOnTLMap.get("todPrice") : (Double)dataOnTLMap.get("price");
				if(priceOnTl.doubleValue() > price.doubleValue()) continue;

				price -= priceOnTl;
				if(price.doubleValue() <= minimal) {
					priceOnTl += price;
					price = 0.0;
					LOG.debug("Less than or equal with minimal value");
				}

				if(tab.equals("1")) {
					//---: Reform 3 bon and tod.
					todPriceFormated = "";
					todPrice = todPriceManage(orderNumer, chkTodOrderMap, priceOnTl);
					if(todPrice > 0) {
						todPriceFormated = "x" + String.format("%,.0f", todPrice);
					}
				}

				ordFormated = label + orderNumer + " = " + String.format("%,.0f", priceOnTl) + todPriceFormated + "\n";

				ordMap = new HashMap<>();
				ordMap.put("ordFormated", ordFormated);
				ordMap.put("orderNumer", orderNumer);
				ordMap.put("price", priceOnTl);
				ordMap.put("todPrice", todPrice);

				ordListMap.add(ordMap);
			}

			if(price > 0) {
				LOG.debug("Price still more than 0");

				if(tab.equals("1")) {
					//---: Reform 3 bon and tod.
					todPriceFormated = "";
					todPrice = todPriceManage(orderNumer, chkTodOrderMap, -1);
					if(todPrice > 0) {
						todPriceFormated = "x" + String.format("%,.0f", todPrice);
					}
				}

				ordFormated = label + orderNumer + " = " + String.format("%,.0f", price) + todPriceFormated + "\n";

				ordMap = new HashMap<>();
				ordMap.put("ordFormated", ordFormated);
				ordMap.put("orderNumer", orderNumer);
				ordMap.put("price", price);
				ordMap.put("todPrice", todPrice);

				ordListMap.add(ordMap);
			} else {
				if(tab.equals("1")) {
					//---: Reform 3 bon and tod.
					todPriceFormated = "";
					todPrice = todPriceManage(orderNumer, chkTodOrderMap, -1);

					if(todPrice > 0) {
						LOG.debug("Get last item to update TOD.");
						ordMap = ordListMap.get(ordListMap.size() - 1);

						todPrice = todPrice + (double)ordMap.get("todPrice");
						todPriceFormated = "x" + String.format("%,.0f", todPrice);

						ordFormated = label + orderNumer + " = " + String.format("%,.0f", (double)ordMap.get("price")) + todPriceFormated + "\n";
						ordMap.put("ordFormated", ordFormated);
						ordMap.put("todPrice", todPrice);
					}
				}
			}

			return ordListMap;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private Double todPriceManage(String ordNumer, Map<String, Object> chkTodOrderMap, double moveOutPrice) throws Exception {
		try {
			List<String> prob = OrderNumberUtil.getOrderNumProb(ordNumer);
			double todPrice;

			for (String ord : prob) {
				if(chkTodOrderMap.containsKey(ord)) {
					todPrice = (double)chkTodOrderMap.get(ord);
					if(todPrice > 0) {
						if(moveOutPrice == -1) {
							chkTodOrderMap.put(ord, 0.0);
							return todPrice;
						}

						if(todPrice > moveOutPrice) {
							todPrice -= moveOutPrice;
							chkTodOrderMap.put(ord, todPrice);
							return moveOutPrice;
						} else {
							chkTodOrderMap.put(ord, 0.0);
							return todPrice;
						}
					} else {
						return 0.0;
					}
				}
			}

			return 0.0;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private byte[] generateFile(Date periodDate, Receiver receiver,
				List<Map> bon3,
				List<Map> bon2,
				List<Map> lang2
			) throws Exception {
		try {
			LOG.info("Start Jasper report");
			//--------------------
			String jasperFile = basePath + "/jasper/order.jasper";
			String period = String.format(new Locale("th", "TH"), "%1$td %1$tb %1$tY", periodDate);
			Map<Object, Object> hashMap = new HashMap<>();
			hashMap.put("period", period);
			List<String> group = new ArrayList<>();
			group.add(receiver.getSenderName() + " 3 ตัวตรง");
			group.add(receiver.getSenderName() + " 2 ตัวบน");
			group.add(receiver.getSenderName() + " 2 ตัวล่าง");
			byte[] pdfByte = null, pdfByte2 = null;
			List<Map> listData = null;
			List<String> formatedOrder;

			for (int i = 0; i < group.size(); i++) {
				hashMap.put("title", group.get(i));
				listData = new ArrayList<>();

				if(i == 0) {
					if(bon3.size() == 0) continue;

					formatedOrder = formatedOrder(bon3);

					hashMap.put("order1", formatedOrder.get(0));
					hashMap.put("order2", formatedOrder.get(1));
					listData.add(hashMap);

					//--
					pdfByte = new JasperReportEngine().toPdf(jasperFile, listData, null);
				} else if(i == 1) {
					if(bon2.size() == 0) continue;

					formatedOrder = formatedOrder(bon2);
					hashMap.put("order1", formatedOrder.get(0));
					hashMap.put("order2", formatedOrder.get(1));
					listData.add(hashMap);

					if(pdfByte == null) {
						pdfByte = new JasperReportEngine().toPdf(jasperFile, listData, null);
					} else {
						pdfByte2 = new JasperReportEngine().toPdf(jasperFile, listData, null);
						pdfByte = mergePdf(pdfByte, pdfByte2);
					}
				} else {
					if(lang2.size() == 0) continue;

					formatedOrder = formatedOrder(lang2);
					hashMap.put("order1", formatedOrder.get(0));
					hashMap.put("order2", formatedOrder.get(1));
					listData.add(hashMap);

					if(pdfByte == null) {
						pdfByte = new JasperReportEngine().toPdf(jasperFile, listData, null);
					} else {
						pdfByte2 = new JasperReportEngine().toPdf(jasperFile, listData, null);
						pdfByte = mergePdf(pdfByte, pdfByte2);
					}
				}
			}

			LOG.info("End exportData");
			return zipFile(pdfByte, period);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private void calTotalTodPrice(Map<String, Object> todDataMap, String recId, String ordNumber, Double todPrice) throws Exception {
		try {
			//--: For TOD price calculation.
			Map<String, Object> chkTodOrderMap;
			List<String> chkTodOrder = null;
			if(todDataMap.containsKey(recId)) {
				chkTodOrderMap = (Map)todDataMap.get(recId);
				chkTodOrder = (List)chkTodOrderMap.get("chkTodOrder");
				if(!chkTodOrder.contains(ordNumber)) {
					chkTodOrder.addAll(OrderNumberUtil.getOrderNumProb(ordNumber));
					chkTodOrderMap.put("todPrice", (Double)chkTodOrderMap.get("todPrice") + todPrice);
				}
			} else {
				chkTodOrderMap = new HashMap<>();
				chkTodOrderMap.put("chkTodOrder", OrderNumberUtil.getOrderNumProb(ordNumber));
				chkTodOrderMap.put("todPrice", todPrice);
				todDataMap.put(recId, chkTodOrderMap);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private byte[] zipFile(byte[] byteArr, String period) throws Exception {
		try {
			String tmpDir = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%1$tL", Calendar.getInstance().getTime()) + "/";
			String orderFile = basePath + "/orderfile/" + tmpDir;
			new File(orderFile).mkdir();

			PDDocument document = PDDocument.load(byteArr);
			int numberOfPages = document.getNumberOfPages();
			PDFRenderer renderer = new PDFRenderer(document);
			BufferedImage image;

			for (int i = 0; i < numberOfPages; i++) {
				image = renderer.renderImageWithDPI(i, 300);

				//-- Save to .jpg file
				ImageIO.write(image, "JPEG", new File(orderFile + period + "_" + (i+1) + ".jpg"));
			}

			//-- Save to .pdf file
			FileUtils.writeByteArrayToFile(new File(orderFile + period + ".pdf"), byteArr);

			//-- Create .zip byte[] data
			byte[] zipByte = ZipUtil.createZip(orderFile);

			// Remove all file.
//			FileUtils.cleanDirectory(new File(orderFile));
			FileUtils.deleteDirectory(new File(orderFile));

			return zipByte;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<String> formatedOrder(List<Map> list) {
		try {
			List<String> result = new ArrayList<>();
			StringBuilder formated1 = new StringBuilder();
			StringBuilder formated2 = new StringBuilder();
			String ordFormated;
			int pageIndex = 0;
			int rowSize = 40;
			Map orderMap;

			for (int i = 0; i < list.size(); i++) {
				orderMap = list.get(i);
				ordFormated = orderMap.get("ordFormated").toString();
				if(pageIndex < rowSize) {
					formated1.append(ordFormated);
				} else if(pageIndex < rowSize * 2){
					formated2.append(ordFormated);
				} else {
					pageIndex = 0;
					formated1.append(ordFormated);
				}
				pageIndex++;
			}

			result.add(formated1.toString().trim());
			result.add(formated2.toString().trim());

			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private byte[] mergePdf(byte[] pdfByte1, byte[] pdfByte2) throws Exception {
		try {
			Document document = new Document();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			PdfCopy copy = new PdfSmartCopy(document, outputStream);
			document.open();

			PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfByte1));
			copy.addDocument(reader);

			reader = new PdfReader(new ByteArrayInputStream(pdfByte2));
			copy.addDocument(reader);

			reader.close();
			document.close();

			pdfByte1 = outputStream.toByteArray();

			return pdfByte1;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
