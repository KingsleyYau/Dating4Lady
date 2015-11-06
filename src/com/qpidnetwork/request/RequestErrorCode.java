package com.qpidnetwork.request;

public class RequestErrorCode {
	
	/**************************************************************************************
	 * 公共模块
	 **************************************************************************************/	
	
	/**
	 * 未登录
	 */
	public static final String MBCE0003 = "MBCE0003";
	
	/**************************************************************************************
	 * 公共模块end
	 **************************************************************************************/
	
	/**************************************************************************************
	 * 登录认证模块
	 **************************************************************************************/

	/**
	 * Facebook没有邮箱（需要提交邮箱）
	 */
	public static final String MBCE64001 = "MBCE64001";
	
	/**
	 * Facebook邮箱已注册但未绑定（需要提交密码）
	 */
	public static final String MBCE64002 = "MBCE64002";
	
	/**
	 * Facebook登录（Token无效）
	 */
	public static final String MBCE64005 = "MBCE64005";
	
	/**
	 * 用户名与密码不正确
	 */
	public static final String MBCE1001 = "MBCE1001";

	/**
	 * 会员帐号暂停
	 */
	public static final String MBCE1002 = "MBCE1002";
	
	/**
	 * 帐号被冻结
	 */
	public static final String MBCE1003 = "MBCE1003";
	
	/**
	 * 验证码为空
	 */
	public static final String MBCE1012 = "MBCE1012";
		
	/**
	 * 验证码错误, 分站禁登录
	 */
	public static final String MBCE1013 = "MBCE1013";
	
	/**
	 * 不允许注册
	 */
	public static final String MBCE1004 = "MBCE1004";
		
	/**************************************************************************************
	 * 登录认证模块 end
	 **************************************************************************************/
	
	/**************************************************************************************
	 * EMF模块
	 **************************************************************************************/
	
	/**
	 * 阅读信件详情，信用点不足
	 */
	public static final String MBCE8012 = "MBCE8012";
	
	/**
	 * 余额不足
	 */
	public static final String MBCE10003 = "MBCE10003";
	
	/**
	 * 不可支付信件費用
	 */
	public static final String MBCE10003123 = "MBCE10003123";
	
	/**
	 * 付费私密照不够费用
	 */
	public static final String MBCE62002 = "MBCE62002";
	
	/**
	 * 附件大小不合法(5m)
	 */
	public static final String MBCE65001 = "MBCE65001";
	
	/**
	 * 附件格式不正确(非jpg)
	 */
	public static final String MBCE65004 = "MBCE65004";
	
	/**
	 * 可支付基本信用費用，但不足支付全部費用
	 */
	public static final String MBCE10003123111 = "MBCE1000312311";
	
	/**************************************************************************************
	 * EMF模块 end
	 **************************************************************************************/
	
	/**
	 * 本地错误代码 , 连接超时
	 */
	public static final String LOCAL_ERROR_CODE_TIMEOUT	= "LOCAL_ERROR_CODE_TIMEOUT";
	
	/**
	 * 本地错误代码 , 协议解析错误
	 */
	public static final String LOCAL_ERROR_CODE_PARSEFAIL =	"LOCAL_ERROR_CODE_PARSEFAIL";
	
	/**
	 * 本地错误代码 , facebook登录失败
	 */
	public static final String LOCAL_ERROR_CODE_FACEBOOK_FAIL = "LOCAL_ERROR_CODE_FACEBOOK_FAIL";
	
	/**
	 * 本地错误代码 , 从来未登录
	 */
	public static final String LOCAL_ERROR_CODE_NERVER_LOGIN = "LOCAL_ERROR_CODE_NERVER_LOGIN";
	/**************************************************************************************
	 * EMF模块 end
	 **************************************************************************************/
	
}
