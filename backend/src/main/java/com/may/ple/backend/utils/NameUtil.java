package com.may.ple.backend.utils;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FIRST_NAME_ENG;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FULL_NAME_ENG;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_LAST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_LAST_NAME_ENG;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_SHOWNAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FIRST_NAME_ENG;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FULL_NAME_ENG;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_LAST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_LAST_NAME_ENG;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_SHOWNAME;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class NameUtil {

	public static void traceName(List<Map<String, String>> userOwnerList, Map val, boolean isOwner) {
		if(userOwnerList != null && userOwnerList.size() > 0) {
			Map u = (Map)userOwnerList.get(0);
			String firstName = "", firstNameEng = "", lastName = "", lastNameEng = "", showname;

			showname = u.get("showname").toString();

			firstName = u.get("firstName") != null ? u.get("firstName").toString() : "";
			firstNameEng = u.get("firstNameEng") != null ? u.get("firstNameEng").toString() : "";

			lastName = u.get("lastName") != null ? u.get("lastName").toString() : "";
			lastNameEng = u.get("lastNameEng") != null ? u.get("lastNameEng").toString() : "";

			if(isOwner) {
				val.put(SYS_OWNER_SHOWNAME.getName(), showname);

				val.put(SYS_OWNER_FIRST_NAME.getName(), firstName);
				val.put(SYS_OWNER_FIRST_NAME_ENG.getName(), firstNameEng);

				val.put(SYS_OWNER_LAST_NAME.getName(), lastName);
				val.put(SYS_OWNER_LAST_NAME_ENG.getName(), lastNameEng);

				val.put(SYS_OWNER_FULL_NAME.getName(), (StringUtils.trimToEmpty(firstName) + " " + StringUtils.trimToEmpty(lastName)).trim());
				val.put(SYS_OWNER_FULL_NAME_ENG.getName(), (StringUtils.trimToEmpty(firstNameEng) + " " + StringUtils.trimToEmpty(lastNameEng)).trim());
			} else {
				val.put(SYS_CREATED_SHOWNAME.getName(), showname);

				val.put(SYS_CREATED_FIRST_NAME.getName(), firstName);
				val.put(SYS_CREATED_FIRST_NAME_ENG.getName(), firstNameEng);

				val.put(SYS_CREATED_LAST_NAME.getName(), lastName);
				val.put(SYS_CREATED_LAST_NAME_ENG.getName(), lastNameEng);

				val.put(SYS_CREATED_FULL_NAME.getName(), (StringUtils.trimToEmpty(firstName) + " " + StringUtils.trimToEmpty(lastName)).trim());
				val.put(SYS_CREATED_FULL_NAME_ENG.getName(), (StringUtils.trimToEmpty(firstNameEng) + " " + StringUtils.trimToEmpty(lastNameEng)).trim());
			}
		}
	}

}
