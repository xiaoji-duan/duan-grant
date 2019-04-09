package com.xiaoji.duan.abd.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaoji.duan.abd.entity.ABD_Group;
import com.xiaoji.duan.abd.entity.ABD_GroupSA;
import com.xiaoji.duan.abd.entity.ABD_GroupUser;
import com.xiaoji.duan.abd.service.ABDService;
import com.xiaoji.duan.abd.service.HttpService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/my")
public class MyController {

    @Autowired
    private HttpService httpauth;
    
	@Autowired
	private ABDService abdService;
	
    @Value("${zuul.authorize.url}")
    private URL authurl;
    @Value("${zuul.authorize.path}")
    private String authpath;

    @RequestMapping("{subdomain}/{prefix}/{openid}/roles")
    public Map<String, Object> getRoles(@PathVariable String subdomain, @PathVariable String prefix, @PathVariable String openid) {
		Map<String, Object> rslt = new HashMap<String, Object>();
		
		rslt.put("code", "");
		rslt.put("message", "");
		
		Object data = this.abdService.getGroupSAUser(subdomain, prefix, openid);

		if (data == null) {
			data = new HashMap<String, Object>();
		}
		
		rslt.put("data", data);
		
		return rslt;
    }

	@RequestMapping("{subdomain}/username")
	public Map<String, Object> getUserName(HttpServletRequest req, @PathVariable String subdomain, @RequestParam Map<String, String> params, @RequestBody(required = false) Map<String, Object> bodyparams) {
		String username = params.get("username");
		
		String token = params.get("token");
		String openid = params.get("openid");
		
		if (bodyparams != null) {
			if (token == null || "".equals(token)) {
				if (bodyparams.get("token") != null) {
					token = ((ArrayList<String>) bodyparams.get("token")).get(0);
				}
			}
			
			if (openid == null || "".equals(openid)) {
				if (bodyparams.get("openid") != null) {
					openid = ((ArrayList<String>) bodyparams.get("openid")).get(0);
				}
			}
		}
		
		Cookie[] cookies = req.getCookies();
		if (cookies != null && (token == null || "".equals(token) || openid == null || "".equals(openid))) {
			for (Cookie cookie : cookies) {
				switch(cookie.getName()) {
				case "authorized_user":
    	            token = cookie.getValue();
    	            break;
				case "authorized_openid":
    	            openid = cookie.getValue();
    	            break;
				default:
					break;
				}
			}
		}
		
		Map<String, Object> rslt = new HashMap<String, Object>();
		rslt.put("code", "");
		rslt.put("message", "");
		
		Object data = new HashMap<String, Object>();

		if (token != null && !"".equals(token) && openid != null && !"".equals(openid)) {
			Map<String, Object> res = httpauth.https(authurl + "/" + openid + "/info", new HashMap<String, String[]>());

			if (res != null && res.get("data") != null) {
				data = (Object) res.get("data");
			}
		}

		System.out.println("get username " + subdomain + ", " + openid);

		if (openid != null && !"".equals(openid)) {
			ABD_GroupUser gu = this.abdService.getGroupUser(subdomain, openid);

			if (gu != null) {
				data = JSON.toJSON(gu);

				((JSONObject) data).put("openid", gu.getUserId());
				((JSONObject) data).put("name", gu.getUserName());
			}
		}
		
		rslt.put("data", data);
		
		return rslt;
	}
	
	@RequestMapping("{subdomain}/updateusername")
	public Map<String, Object> updateGroupUserName(HttpServletRequest req, @PathVariable String subdomain, @RequestParam Map<String, String> params) {
		String username = params.get("username");
		
		String token = "";
		String openid = "";
		
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				switch(cookie.getName()) {
				case "authorized_user":
    	            token = cookie.getValue();
    	            break;
				case "authorized_openid":
    	            openid = cookie.getValue();
    	            break;
				default:
					break;
				}
			}
		}
		
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("code", "");
		res.put("message", "");
		
		Object data = new HashMap<String, Object>();

		if (openid != null && !"".equals(openid)) {
			int updated = this.abdService.updateGroupUserName(subdomain, openid, username);
			
			if (updated < 1) {
				res.put("code", "ABD_100004");
				res.put("message", "更新失败.");
			}
		} else {
			res.put("code", "ABD_100003");
			res.put("message", "未获得用户识别号，无法使用该功能.");
		}
		
		res.put("data", data);
		
		return res;
	}
	
	@RequestMapping("groups")
	public Map<String, Object> mygroups(HttpServletRequest req) {
		
		String userId = "";
		
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				switch(cookie.getName()) {
				case "authorized_openid":
					userId = cookie.getValue();
    	            break;
				default:
					break;
				}
			}
		}
				
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("code", "");
		res.put("message", "");
		
		Object data = new HashMap<String, Object>();

		if (userId != null && !"".equals(userId)) {
			List<ABD_Group> groupins = this.abdService.getUserGroupInstances(userId);
			
			if (groupins == null)
				data = new ArrayList<ABD_Group>();
			else
				data = groupins;
		} else {
			res.put("code", "ABD_100001");
			res.put("message", "未登录用户不能访问本功能.");
		}
		
		res.put("data", data);
		
		return res;
	}

	@RequestMapping("{subdomain}/{prefix}/remove")
	public Map<String, Object> removeGroupSA(@PathVariable String subdomain, @PathVariable String prefix) {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("code", "");
		res.put("message", "");
		
		Object data = new HashMap<String, Object>();
		
		int deleted = this.abdService.deleteGroupSA(subdomain, prefix);
		
		if (deleted < 1) {
			res.put("code", "ABD_100007");
			res.put("message", "删除组织(" + subdomain + ")的短应用(" + prefix + ")失败.");
		}
		
		res.put("data", data);
		
		return res;
	}
	
	@RequestMapping("{subdomain}/addgroupsa")
	public Map<String, Object> addGroupSA(@PathVariable String subdomain, HttpServletRequest req) {
		String groupSubDomain = subdomain;
		String groupName = req.getParameter("groupName");
		String groupFullName = req.getParameter("groupFullName");
		String saName = req.getParameter("saName");
		String saPrefix = req.getParameter("saPrefix");

		Map<String, Object> res = new HashMap<String, Object>();
		res.put("code", "");
		res.put("message", "");
		
		Object data = new HashMap<String, Object>();

		ABD_GroupSA newgroupsa = new ABD_GroupSA();
		
		newgroupsa.setGroupSubdomain(groupSubDomain);
		newgroupsa.setGroupName(groupName);
		newgroupsa.setGroupFullname(groupFullName);
		newgroupsa.setSaName(saName);
		newgroupsa.setSaPrefix(saPrefix);
		newgroupsa.setSaAuthorize("AUTHORIZED");
		newgroupsa.setVerify("AUTOVERIFY");
		
		ABD_GroupSA groupsa = this.abdService.getGroupSA(subdomain, saPrefix);
		
		if (groupsa == null) {
			int added = this.abdService.addGroupSA(newgroupsa);
			
			if (added < 1) {
				res.put("code", "ABD_100005");
				res.put("message", "向" + newgroupsa.getGroupName() + "添加短应用(" + groupsa.getSaName() + ")失败.");
			}
		} else {
			res.put("code", "ABD_100006");
			res.put("message", groupsa.getGroupName() + "已存在短应用(" + groupsa.getSaName() + ")，添加失败！");
		}
		res.put("data", data);
		
		return res;
	}
	
	@RequestMapping("{subdomain}/groupsas")
	public Map<String, Object> mygroupsas(@PathVariable String subdomain, HttpServletRequest req) {
		String userId = "";
		
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				switch(cookie.getName()) {
				case "authorized_openid":
					userId = cookie.getValue();
    	            break;
				default:
					break;
				}
			}
		}
				
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("code", "");
		res.put("message", "");
		
		Object data = new HashMap<String, Object>();

		if (userId != null && !"".equals(userId)) {
			List<ABD_GroupSA> groupsains = this.abdService.getUserGroupSAInstances(subdomain, userId);
			
			if (groupsains == null)
				data = new ArrayList<ABD_GroupSA>();
			else
				data = groupsains;
		} else {
			res.put("code", "ABD_100002");
			res.put("message", "未登录用户不能访问本功能.");
		}
		
		res.put("data", data);
		
		return res;
	}
}
