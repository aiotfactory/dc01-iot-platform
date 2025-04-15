package com.zyc.dc.util;

import com.zyc.dc.dao.Login;

public class CacheUtil {
	public static final ThreadLocal<Login> threadlocallogin = new ThreadLocal<Login>();
}
