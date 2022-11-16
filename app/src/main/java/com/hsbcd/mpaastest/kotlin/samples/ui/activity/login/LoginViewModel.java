package com.hsbcd.mpaastest.kotlin.samples.ui.activity.login;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSON;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.notify.NotifyKickDevice;
import com.alipay.fc.ccmimplus.common.service.facade.enums.BizTypeEnum;

import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.AuthInfo;
import com.alipay.fc.ccmimplus.sdk.core.client.InitConfig;
import com.alipay.fc.ccmimplus.sdk.core.connection.Connection;
import com.alipay.fc.ccmimplus.sdk.core.connection.ConnectionListener;
import com.alipay.fc.ccmimplus.sdk.core.connection.InitListener;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.reducer.MessageReducerManager;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.MsgUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.OkHttpDns;

import com.hsbcd.mpaastest.kotlin.samples.app.ImplusApplication;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.model.ConnectionResult;
import com.hsbcd.mpaastest.kotlin.samples.model.EnvInfo;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.hsbcd.mpaastest.kotlin.samples.model.TntInfo;

/**
 * 登录页ViewModel
 *
 * @author liyalong
 * @version LoginActivity.java, v 0.1 2022年07月28日 19:05 liyalong
 */
public class LoginViewModel extends ViewModel {

    private MutableLiveData<LiveDataResult> loginResultData = new MutableLiveData<>();

    private MutableLiveData<ConnectionResult> connectionResultData = new MutableLiveData<>();

    /**
     * @param activity
     * @param userId
     */
    public void login(LoginActivity activity, String userId) {
        AsyncExecutorService.getInstance().execute(() -> {
            // 1. 销毁之前的连接
            AlipayCcmIMClient.getInstance().destroy();

            // 2. 构建初始化配置
            InitConfig initConfig = buildInitConfig(activity, userId);

            // 3. 注册消息变换器，用于根据客户端设置的oss代理地址替换消息中的oss下载链接
            MessageReducerManager.getInstance().addMessageReducer(
                    (contentType, content) -> MsgUtils.replaceMultipartMessageContentUrl(contentType, content));

            // 4. 执行初始化操作
            AlipayCcmIMClient.getInstance().init(activity, initConfig, new InitListener() {
                @Override
                public void onInitSuccess(AuthInfo authInfo) {
                    Log.i(LoggerName.UI, "登录成功：" + JSON.toJSONString(authInfo));

                    LiveDataResult result = new LiveDataResult(true);
                    loginResultData.postValue(result);
                }

                @Override
                public void onInitFailure(AuthInfo authInfo) {
                    String msg = String.format("登录失败: %s/%s", authInfo.getResultCode(),
                            authInfo.getResultMessage());
                    Log.e(LoggerName.UI, msg);

                    LiveDataResult result = new LiveDataResult(false, msg);
                    loginResultData.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("登录异常: %s/%s/%s", errorCode, message, t.getMessage());
                    Log.e(LoggerName.UI, msg, t);

                    LiveDataResult result = new LiveDataResult(false, msg);
                    loginResultData.postValue(result);
                }
            }, new ConnectionListener() {
                @Override
                public void onActive(Connection connection) {
                    String msg = connection.isReConnect() ? "自动重连成功" : "连接成功";
                    Log.i(LoggerName.UI, msg);

                    ConnectionResult result = new ConnectionResult();
                    result.setEvent(
                            connection.isReConnect() ? ConnectionResult.Event.RECONNECT_OK : ConnectionResult.Event.CONNECT_OK);
                    result.setMessage(msg);

                    connectionResultData.postValue(result);
                }

                /**
                 * 连接中断后回调
                 *
                 * @param connection 连接对象
                 */
                public void onInActive(Connection connection) {
                    String msg = "连接已断开";
                    Log.i(LoggerName.UI, msg);

                    ConnectionResult result = new ConnectionResult();
                    result.setEvent(ConnectionResult.Event.DISCONNECT);
                    result.setMessage(msg);

                    connectionResultData.postValue(result);
                }

                /**
                 * 连接异常回调
                 *
                 * @param connection 连接对象
                 * @param t          异常
                 */
                public void onException(Connection connection, Throwable t) {
                    String msg = String.format("连接异常: %s", t.getMessage());
                    Log.e(LoggerName.UI, msg, t);

                    ConnectionResult result = new ConnectionResult();
                    result.setEvent(ConnectionResult.Event.EXCEPTION);
                    result.setMessage(msg);

                    connectionResultData.postValue(result);
                }

                @Override
                public void onKicked(Connection connection, NotifyKickDevice notifyKickDevice) {
                    connection.registerKickTask(() -> {
                        String msg = String.format("当前设备被踢出, 时间(%s)", notifyKickDevice.getKickTime());
                        Log.w(LoggerName.UI, msg);

                        // 清理当前连接
                        AlipayCcmIMClient.getInstance().invalidate();

                        ConnectionResult result = new ConnectionResult();
                        result.setEvent(ConnectionResult.Event.KICKED);
                        result.setMessage(msg);

                        connectionResultData.postValue(result);
                    });
                }
            });
        });
    }

    /**
     * @param activity
     * @param userId
     * @return
     */
    private InitConfig buildInitConfig(LoginActivity activity, String userId) {
        EnvInfo envInfo = ImplusApplication.Companion.getCurrentEnvInfo();
        TntInfo tntInfo = ImplusApplication.Companion.getCurrentTntInfo();

        InitConfig initConfig = InitConfig.create(activity);

        initConfig.userId(userId);

        // 租户配置
        initConfig.tntInstId(tntInfo.getTntInstId());
        initConfig.appId(tntInfo.getAppId());
        initConfig.envId(tntInfo.getEnvId());
        initConfig.accessKey(tntInfo.getAccessKey());
        initConfig.bizType(BizTypeEnum.IMPLUS.getCode());

        // 服务端websocket配置
        // 解析：ws://[hostname]:[port]
        String[] wsHostArray = envInfo.getWsHost().split("://");
        String[] wsHostSubArrays = wsHostArray[1].split(":");
        initConfig.setWsDebugHost(wsHostSubArrays[0]);
        initConfig.setWsDebugPort(Integer.valueOf(wsHostSubArrays[1]));
        initConfig.setUseSecureWebSocket(wsHostArray[0].equalsIgnoreCase("wss"));

        // 服务端http配置
        initConfig.setEndpoint(envInfo.getHttpHost() + "/openapi/getway.json");

        // 如果客户端环境无法直接访问服务端配置的oss地址，需要在这里设置oss代理地址
        initConfig.setMedeaHost(envInfo.getMediaHost());

        // 支持http请求时自定义域名解析，作用等同于在手机端绑定hosts，一般用于测试环境
        if (CollectionUtils.isNotEmpty(envInfo.getHostMappings())) {
            initConfig.setTestServerMode(true);
            envInfo.getHostMappings().forEach(m -> OkHttpDns.addHostMapping(m.getIp(), m.getHostname()));
        }

        return initConfig;
    }

    public MutableLiveData<LiveDataResult> getLoginResultData() {
        return loginResultData;
    }

    public MutableLiveData<ConnectionResult> getConnectionResultData() {
        return connectionResultData;
    }
}