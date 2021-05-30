package com.may.ple.backend.action;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.criteria.DealerCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.entity.Dealer;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.AuthenticationRequest;
import com.may.ple.backend.model.AuthenticationResponse;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.security.CerberusUser;
import com.may.ple.backend.security.CerberusUserFactory;
import com.may.ple.backend.security.TokenUtils;
import com.may.ple.backend.service.DealerService;
import com.may.ple.backend.service.OrderService;
import com.may.ple.backend.service.SendRoundService;
import com.may.ple.backend.utils.ImageUtil;

@RestController
public class LoginAction {
	private static final Logger LOG = Logger.getLogger(LoginAction.class.getName());
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private TokenUtils tokenUtils;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MongoTemplate template;
	@Autowired
	private DealerService dealer;
	@Autowired
	private OrderService orderService;
	@Autowired
	private SendRoundService sendRoundService;

	@Autowired
    ServletContext servletContext;
	@Value("${backend.version}")
	String version;

	@RequestMapping(value="/login", method = RequestMethod.POST)
	public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest, Device device) throws Exception {
		AuthenticationResponse resp;

		try {
			LOG.debug("Start Login");
		    Authentication authentication = authenticationManager.authenticate(
		    		new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), new String(Base64.decode(authenticationRequest.getPassword().getBytes())))
		    );

		    List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>)authentication.getAuthorities();
		    RolesConstant rolesConstant = RolesConstant.valueOf(authorities.get(0).getAuthority());
		    if(rolesConstant == RolesConstant.ROLE_AGENT) {
		    	throw new Exception("Agent Role not allow to access backend.");
		    }

		    SecurityContextHolder.getContext().setAuthentication(authentication);

		    UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken)authentication;
		    CerberusUser cerberusUser = (CerberusUser)authToken.getPrincipal();

		    String token = tokenUtils.generateToken(cerberusUser, device);

		    if(cerberusUser.getPhoto() == null) {
		    	LOG.debug("Use default thumbnail");
		    	cerberusUser.setPhoto(ImageUtil.getDefaultThumbnail(servletContext));
		    }

		    resp = new AuthenticationResponse(token, cerberusUser.getId(), cerberusUser.getShowname(), cerberusUser.getUsername(), cerberusUser.getAuthorities(), cerberusUser.getPhoto());
		    String companyName = getCompanyName();

		    resp.setDealers(getDealer(cerberusUser.getDealerId()));
		    resp.setServerDateTime(new Date());
		    resp.setFirstName(cerberusUser.getFirstName());
		    resp.setLastName(cerberusUser.getLastName());
		    resp.setTitle(cerberusUser.getTitle());
		    resp.setCompanyName(companyName);
		    resp.setVersion(version);

		    LOG.debug("End Login");
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}

	@RequestMapping(value="/refreshToken", method = RequestMethod.POST)
	public ResponseEntity<?> refreshToken(@RequestBody AuthenticationRequest authenticationRequest, Device device) throws Exception {
		AuthenticationResponse resp;

		try {
			LOG.debug("Start refreshToken");
			String token = tokenUtils.refreshToken(authenticationRequest.getToken());

			if(token == null) {
				return ResponseEntity.status(401).build();
			}

			String username = tokenUtils.getUsernameFromToken(token);
			Users user = userRepository.findByUsername(username);

			if(!user.getEnabled()) {
				LOG.debug("User is inactive");
				return ResponseEntity.status(410).build();
			}

			byte[] photo = null;
			if(user.getImgData() != null && user.getImgData().getImgContent() != null) {
				photo = user.getImgData().getImgContent();
			} else {
				LOG.debug("Use default thumbnail");
				photo = ImageUtil.getDefaultThumbnail(servletContext);
			}

			resp = new AuthenticationResponse(token, user.getId(), user.getShowname(), user.getUsername(), user.getAuthorities(), photo);

			String companyName = getCompanyName();

			resp.setDealers(getDealer(user.getDealerId()));
			resp.setServerDateTime(new Date());
			resp.setFirstName(user.getFirstName());
		    resp.setLastName(user.getLastName());
		    resp.setTitle(user.getTitle());
		    resp.setCompanyName(companyName);
		    resp.setVersion(version);

		    LOG.debug("End refreshToken");
		    return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}

	@RequestMapping(value="/loginByLineUserId", method = RequestMethod.POST)
	public ResponseEntity<?> loginByLineUserId(@RequestBody AuthenticationRequest authenticationRequest, Device device) throws Exception {
		AuthenticationResponse resp;

		try {
			String lineUserId = new String(Base64.decode(authenticationRequest.getLineUserId().getBytes()));
			if(StringUtils.isBlank(lineUserId)) throw new Exception("lineUserId is blank.");

			Users user = userRepository.findByLineUserId(lineUserId);
			if(user == null) {
				resp = new AuthenticationResponse();
				resp.setUserNotFoundErr(true);
				return ResponseEntity.ok(resp);
			}

			LOG.info("Login by : " + user.getShowname() + ", ID: " + user.getId());
			CerberusUser cerberusUser = CerberusUserFactory.create(user);

			LOG.debug("Start loginByLineUserId");
			String token = tokenUtils.generateToken(cerberusUser, device);

			if(token == null) {
				return ResponseEntity.status(401).build();
			}

			if(!user.getEnabled()) {
				LOG.debug("User is inactive");
				return ResponseEntity.status(410).build();
			}

			resp = new AuthenticationResponse(token, user.getId(), user.getShowname(), user.getUsername(), user.getAuthorities(), null);

			Map periodMap = orderService.getPeriod().get(0);
			periodMap.put("_id", periodMap.get("_id").toString());

			List<Dealer> dealers = getDealer(user.getDealerId());
			Dealer dealer = dealers.get(0);

			if(dealer.getOrderImg() != null && dealer.getOrderImg()) {
				LOG.debug("Get Order File");
				resp.setOrderFile(orderService.getOrderFile(periodMap.get("_id").toString(), user.getDealerId(), null, user.getUsername(), 1));
			}

			if(user.getAuthorities().get(0).getAuthority().equals("ROLE_ADMIN")) {
				resp.setSendRoundList(sendRoundService.getDataList(true, dealer.getId()));
			}

			resp.setPeriod(periodMap);
			resp.setDealers(dealers);
			resp.setServerDateTime(new Date());
			resp.setFirstName(user.getFirstName());
			resp.setLastName(user.getLastName());
			resp.setTitle(user.getTitle());
			resp.setVersion(version);

			LOG.debug("End loginByLineUserId");
			return ResponseEntity.ok(resp);
		} catch (BadCredentialsException e) {
			LOG.error(e.toString());
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}

	private List<Dealer> getDealer(String id) {
	    DealerCriteriaReq dealerReq = new DealerCriteriaReq();
	    Dealer d = new Dealer();
	    d.setId(id);
	    d.setEnabled(true);
	    dealerReq.setDealer(d);
	    return dealer.getDealer(dealerReq);
	}

	private String getCompanyName() {
		ApplicationSetting find = template.findOne(new Query(), ApplicationSetting.class);
		return find == null ? null : find.getCompanyName();
	}

	private ApplicationSetting getAppSetting() {
		return template.findOne(new Query(), ApplicationSetting.class);
	}

}
