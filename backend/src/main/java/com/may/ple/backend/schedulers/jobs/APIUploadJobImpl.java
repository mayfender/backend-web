package com.may.ple.backend.schedulers.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.ibm.icu.util.Calendar;
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

@Component
public class APIUploadJobImpl {
	private static final Logger LOG = Logger.getLogger(APIUploadJobImpl.class.getName());
	private boolean isInprogress = false;
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
	}

	public void proceed() {
		if(isInprogress) {
			LOG.info("Still in progress.");
			return;
		}

		//---:
		isInprogress = true;
		new JobProcess().start();
	}

	//---:
	class JobProcess extends Thread {
		@Override
		public void run() {
			try {
				LOG.info("Start");

				List<Product> prods = templateCore.find(Query.query(Criteria.where("enabled").is(1)), Product.class);

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

				ProductSetting productSetting;
				TraceSaveCriteriaReq req;
				Map krungSriAPISetting;
				MongoTemplate template;
				List<DymList> dymList;
				List<Map> dymListVal;
				Criteria criteria;
				List<Map> traces;
				Map taskDetail;
				Query query;
				Users user;
				Map dymMap;

				for (Product product : prods) {
					productSetting = product.getProductSetting();
					krungSriAPISetting = productSetting.getKrungSriAPI();

					if(krungSriAPISetting == null || krungSriAPISetting.get("enable") == null || (int)krungSriAPISetting.get("enable") == 0) {
						continue;
					}

					DymListFindCriteriaReq dymReq = new DymListFindCriteriaReq();
					dymReq.setProductId(product.getId());
					ArrayList<Integer> statuses = new ArrayList<>();
					statuses.add(1);
					dymReq.setStatuses(statuses);
					dymList = dymService.findList(dymReq);

					LOG.info("Start to look on product: " + product.getProductName());
					template = dbFactory.getTemplates().get(product.getId());

					criteria = Criteria.where("createdDateTime").gte(fromDate).lte(toDate).and("uploadStatusCode").is("DMS_100");
					query = Query.query(criteria);

					traces = template.find(query, Map.class, "traceWorkAPIUpload");

					for (Map trace : traces) {
						System.out.println(trace);

						req = new TraceSaveCriteriaReq();
						req.setProductId(product.getId());
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
						traceService.save(req, user);

						Thread.sleep(1000);
					}
				}

	            LOG.info("End");
			} catch (Exception e) {
				LOG.error(e.toString(), e);
			} finally {
				isInprogress = false;
			}
		}
	}

}
