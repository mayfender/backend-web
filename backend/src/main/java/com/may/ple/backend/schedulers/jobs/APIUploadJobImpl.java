package com.may.ple.backend.schedulers.jobs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.criteria.DymListFindCriteriaReq;
import com.may.ple.backend.criteria.TraceSaveCriteriaReq;
import com.may.ple.backend.entity.DymList;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.service.DymListService;
import com.may.ple.backend.service.TraceWorkService;
import com.may.ple.backend.service.UserService;
import com.may.ple.backend.utils.WorkingTimeUtil;

@Component
public class APIUploadJobImpl {
	private static final Logger LOG = Logger.getLogger(APIUploadJobImpl.class.getName());
	private Map<String, Boolean> processStatus;
	private TraceWorkService traceService;
	private MongoTemplate templateCore;
	private DymListService dymService;
	private UserService userService;
	private DbFactory dbFactory;

	@Autowired
	public APIUploadJobImpl(MongoTemplate templateCore, DbFactory dbFactory,
								TraceWorkService traceService, UserService userService, DymListService dymService) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.traceService = traceService;
		this.userService = userService;
		this.dymService = dymService;
		processStatus = new HashMap<>();
	}

	public void proceed() {
		List<Product> prods = templateCore.find(Query.query(Criteria.where("enabled").is(1)), Product.class);
		ProductSetting productSetting;
		Map krungSriAPISetting;

		for (Product product : prods) {
			if(processStatus.get(product.getId()) != null && processStatus.get(product.getId())) {
				LOG.info("Still in progress.");
				return;
			}

			productSetting = product.getProductSetting();
			krungSriAPISetting = productSetting.getKrungSriAPI();

			if(krungSriAPISetting == null || krungSriAPISetting.get("enable") == null || (int)krungSriAPISetting.get("enable") == 0) {
				continue;
			}

			LOG.info("Start on product: " + product.getProductName());
			processStatus.put(product.getId(), true);
			new JobProcess(product).start();
		}
	}

	//---:
	class JobProcess extends Thread {
		private Product product;

		public JobProcess(Product product) {
			this.product = product;
		}

		@Override
		public void run() {
			try {
				ProductSetting productSetting = product.getProductSetting();
				Map krungSriAPISetting = productSetting.getKrungSriAPI();

				if(krungSriAPISetting == null || krungSriAPISetting.get("enable") == null || (int)krungSriAPISetting.get("enable") == 0) {
					return;
				}

				LOG.info("Start");
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				Date fromDate = cal.getTime();

				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				Date toDate = cal.getTime();

				DymListFindCriteriaReq dymReq = new DymListFindCriteriaReq();
				dymReq.setProductId(product.getId());
				List<Integer> statuses = new ArrayList<>();
				statuses.add(1);
				dymReq.setStatuses(statuses);
				List<DymList> dymList = dymService.findList(dymReq);

				//---:
				MongoTemplate template = dbFactory.getTemplates().get(product.getId());
				Criteria criteria = Criteria.where("createdDateTime").gte(fromDate).lte(toDate);
				Query query = Query.query(criteria);
				query.limit(300);
				List<Map> traces = template.find(query, Map.class, "traceWorkAPIUpload");

				LOG.info("traces size: " + traces.size());
				if(traces.size() == 0) {
					LOG.info("Today's traceWorkAPIUpload is empty.");
					return;
				}

				Calendar today = Calendar.getInstance();
				int hod = today.get(Calendar.HOUR_OF_DAY);
				boolean isOverTime = isOverWorkingTime(productSetting);
				if(isOverTime) {
					LOG.info("Overtime");
					if(hod > 12) {
						LOG.info("Move All to do on tomorrow.");
						Calendar tomorow = Calendar.getInstance();
						tomorow.add(Calendar.DAY_OF_MONTH, 1);

						Update update = new Update();
						update.set("createdDateTime", tomorow.getTime());
						template.updateMulti(query, update, "traceWorkAPIUpload");
					}
					return;
				}

				LOG.info("Call manageTrace");
				manageTrace(template, traces, dymList, product.getId(), productSetting);

	            LOG.info("End");
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			} finally {
				processStatus.remove(product.getId());
			}
		}

		private void manageTrace(MongoTemplate template, List<Map> traces, List<DymList> dymList, String productId, ProductSetting productSetting) throws Exception {
			try {
				LOG.info("On time");

				Map taskDetail;
				Map<String, List<Map>> traceGroup = new HashMap<>();
				List<Map> traceList;
				String userId;

				for (Map trace : traces) {
					taskDetail = (Map)trace.get("taskDetail");
					userId = ((List)taskDetail.get("sys_owner_id")).get(0).toString();

					if(traceGroup.containsKey(userId)) {
						traceList = traceGroup.get(userId);
						traceList.add(trace);
					} else {
						traceList = new ArrayList<>();
						traceList.add(trace);
						traceGroup.put(userId, traceList);
					}
				}

				//---:
				long waiting[] = new long[] {2, 3}; //2, 3 minutes, respectively.
				List<Map> readyTraces = new ArrayList<>();
				int random;
				while(true) {
					readyTraces.clear();
					traceGroup.forEach((k, v) -> {
						if(v.size() > 0) {
							readyTraces.add(v.get(0));
							v.remove(0);
						}
					});

					if(readyTraces.size() ==  0) {
						LOG.info("No traces to upload.");
						break;
					}

					//---:
					boolean isOverTime = isOverWorkingTime(productSetting);
					if(isOverTime) {
						LOG.info("Over Time !");
						break;
					}

					//---:
					new Thread("Upload") {
						@Override
						public void run() {
							try {
								if(readyTraces.size() > 2) {
									int random = getRandom(0, readyTraces.size() - 2);
									readyTraces.add(random, readyTraces.remove(readyTraces.size() - 1));
								}

								List<ObjectId> uploadedTID = saveTrace(readyTraces, dymList, productId);
								if(uploadedTID.size() > 0) {
									template.remove(Query.query(Criteria.where("_id").in(uploadedTID)), "traceWorkAPIUpload");
								}
							} catch (Exception e) {
								LOG.error(e.toString(), e);
							}
						}
					}.start();

					//---:
					random = getRandom(0, waiting.length - 1);
					LOG.info("Trace-Group array index is " + random);
					Thread.sleep(60000 * waiting[random]);
				}

				LOG.info("End manageTrace");
			} catch (Exception e) {
				LOG.error(e.toString(), e);
				throw e;
			}
		}

		private List<ObjectId> saveTrace(List<Map> traces, List<DymList> dymList, String productId) throws Exception {
			try {
				long waiting[] = new long[] {3, 4, 5, 6, 7}; // any seconds, respectively.
				List<ObjectId> traceWorkAPIUpload = new ArrayList<>();
				TraceSaveCriteriaReq req;
				List<Map> dymListVal;
				Map taskDetail;
				Map dymMap;
				Users user;
				int random;

				for (Map trace : traces) {
					req = new TraceSaveCriteriaReq();
					req.setProductId(productId);
					req.setContractNo(trace.get("contractNo").toString());
					req.setResultText(trace.get("resultText").toString());
					req.setTel(trace.get("tel") == null ? null : trace.get("tel").toString());
					req.setAppointDate(trace.get("appointDate") == null ? null : (Date)trace.get("appointDate"));
					req.setNextTimeDate(trace.get("nextTimeDate") == null ? null : (Date)trace.get("nextTimeDate"));
					req.setAppointAmount(trace.get("appointAmount") == null ? null : (Double)trace.get("appointAmount"));

					taskDetail = (Map)trace.get("taskDetail");
					req.setTaskDetailId(taskDetail.get("_id").toString());

					dymListVal = new ArrayList<>();
					for (DymList dym : dymList) {
						if(trace.get(dym.getFieldName()) == null) continue;
						dymMap = new HashMap<>();
						dymMap.put("fieldName", dym.getFieldName());
						dymMap.put("value", trace.get(dym.getFieldName()));
						dymListVal.add(dymMap);
					}
					req.setDymListVal(dymListVal);

					//---:
					user = userService.getUserById(((List)taskDetail.get("sys_owner_id")).get(0).toString(), "showname");
					traceService.save(req, user, trace.get("fileId").toString());
					traceWorkAPIUpload.add((ObjectId)trace.get("_id"));

					//---:
					random = getRandom(0, waiting.length - 1);
					LOG.info("Trace array index is " + random);
					Thread.sleep(1000 * waiting[random]);
				}

				return traceWorkAPIUpload;
			} catch (Exception e) {
				LOG.error(e.toString());
				throw e;
			}
		}

		public int getRandom(int min, int max) {
			max++;
			return new Random().nextInt(max - min) + min;
		}

	}

	private boolean isOverWorkingTime(ProductSetting productSetting) {
		Integer workingTimeCalculation = WorkingTimeUtil.workingTimeCalculation(productSetting, RolesConstant.ROLE_USER);

		//---: Before real overtime 30 minutes. (1800 second)
		if(workingTimeCalculation == null || workingTimeCalculation < 1800) {
			return true;
		} else {
			return false;
		}
	}

}
