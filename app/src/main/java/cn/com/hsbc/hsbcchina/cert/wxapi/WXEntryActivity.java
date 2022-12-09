package cn.com.hsbc.hsbcchina.cert.wxapi;

import static com.hsbcd.mpaastest.kotlin.samples.constants.Constant.WX_UNION_ID;
import static com.hsbcd.mpaastest.kotlin.samples.constants.WeChat.WX_APP_ID;
import static com.hsbcd.mpaastest.kotlin.samples.constants.WeChat.WX_APP_SECRET;
import static com.hsbcd.mpaastest.kotlin.samples.util.ExtensionsKt.applySchedulers;
import static com.hsbcd.mpaastest.kotlin.samples.util.ExtensionsKt.md5;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hsbcd.mpaastest.kotlin.samples.http.NFTApi;
import com.hsbcd.mpaastest.kotlin.samples.http.NFTRetrofit;
import com.hsbcd.mpaastest.kotlin.samples.http.model.nft.RegisterRequest;
import com.hsbcd.mpaastest.kotlin.samples.http.model.nft.RegisterResponse;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.login.LoginActivity;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.List;

import cn.com.hsbc.hsbcchina.cert.R;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	private static String TAG = "MicroMsg.WXEntryActivity";

    private IWXAPI api;
	private static MyHandler handler;
	private static String openId, accessToken, refreshToken, scope;

	private static class MyHandler extends Handler {
		private final WeakReference<WXEntryActivity> wxEntryActivityWeakReference;

		public MyHandler(WXEntryActivity wxEntryActivity){
			wxEntryActivityWeakReference = new WeakReference<WXEntryActivity>(wxEntryActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			int tag = msg.what;
			Bundle data = msg.getData();
			JSONObject json = null;
			switch (tag) {
				case NetworkUtil.GET_TOKEN: {
					try {
						json = new JSONObject(data.getString("result"));
						openId = json.getString("openid");
						accessToken = json.getString("access_token");
						refreshToken = json.getString("refresh_token");
						scope = json.getString("scope");
						String text = String.format("openId:%s, accessToken:%s, refreshToken:&s, scope:%s",openId, accessToken,refreshToken,scope);
						Toast.makeText(wxEntryActivityWeakReference.get(), text, Toast.LENGTH_LONG).show();
						Log.i(TAG, text);
						NetworkUtil.sendWxAPI(handler, String.format("https://api.weixin.qq.com/sns/auth?" +
								"access_token=%s&openid=%s", accessToken, openId), NetworkUtil.CHECK_TOKEN);
//						{"access_token":"63_ceR6R3lKFLwJAXOjlKtMWLhyQZ-qCBiqY-CO-2fF6SoY5rxv1soVSz32GdBfjWAH0W3d9fge1rajm6ilil1Gq9AkyT-VjAI5AydYH06tAsE","expires_in":7200,"refresh_token":"63_sF6hli7j-J-y4ogTXIkk8D5BM0lH71fFXzssi-m8dJneURZf9IlWvQqAdSenxPCX9ZVXwBe2nk0EMjHOupQOrzuXm_1goshaqcfLpcbjezQ","openid":"oBeDs1Uglid37LCCnV5__i04XB_U","scope":"snsapi_userinfo","unionid":"o-uxWwVmXta7bMjEbHT69Qnnj3I8"}
//						Intent intent = new Intent(wxEntryActivityWeakReference.get(), SendToWXActivity.class);
//						intent.putExtra("openId", openId);
//						intent.putExtra("accessToken", accessToken);
//						intent.putExtra("refreshToken", refreshToken);
//						intent.putExtra("scope", scope);
//						wxEntryActivityWeakReference.get().startActivity(intent);
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					}
				}
				case NetworkUtil.CHECK_TOKEN: {
					try {
						json = new JSONObject(data.getString("result"));
						int errcode = json.getInt("errcode");
						if (errcode == 0) {
							NetworkUtil.sendWxAPI(handler, String.format("https://api.weixin.qq.com/sns/userinfo?" +
									"access_token=%s&openid=%s", accessToken, openId), NetworkUtil.GET_INFO);
						} else {
							NetworkUtil.sendWxAPI(handler, String.format("https://api.weixin.qq.com/sns/oauth2/refresh_token?" +
											"appid=%s&grant_type=refresh_token&refresh_token=%s", "wxd930ea5d5a258f4f", refreshToken),
									NetworkUtil.REFRESH_TOKEN);
						}
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					}
					break;
				}
				case NetworkUtil.REFRESH_TOKEN: {
					try {
						json = new JSONObject(data.getString("result"));
						openId = json.getString("openid");
						accessToken = json.getString("access_token");
						refreshToken = json.getString("refresh_token");
						scope = json.getString("scope");
						NetworkUtil.sendWxAPI(handler, String.format("https://api.weixin.qq.com/sns/userinfo?" +
								"access_token=%s&openid=%s", accessToken, openId), NetworkUtil.GET_INFO);
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					}
					break;
				}
				case NetworkUtil.GET_INFO: {
					try {
						json = new JSONObject(data.getString("result"));
						final String nickname, sex, province, city, country, headimgurl, unionID;
						headimgurl = json.getString("headimgurl");
						unionID = json.getString("unionid");
//						NetworkUtil.getImage(handler, headimgurl, NetworkUtil.GET_IMG);
						String encode;
						encode = getcode(json.getString("nickname"));
						nickname = "test1"+new String(json.getString("nickname").getBytes(encode), "utf-8");
						sex = json.getString("sex");
						province = json.getString("province");
						city = json.getString("city");
						country = json.getString("country");
						Log.i(TAG,"nickname = "+nickname+",headimgurl="+headimgurl+
								",sex = "+sex+",province="+province+
								",city="+city+",country="+country);
						registerNewUser(unionID, nickname, headimgurl);
//						handler.post(new Runnable() {
//							@Override
//							public void run() {
//								((TextView)wxEntryActivityWeakReference.get().findViewById(R.id.nickname)).setText(nickname);
//								((TextView)userInfoActivityWR.get().findViewById(R.id.scope)).setText(scope);
//								((TextView)userInfoActivityWR.get().findViewById(R.id.sex)).setText(sex);
//								((TextView)userInfoActivityWR.get().findViewById(R.id.province)).setText(province);
//								((TextView)userInfoActivityWR.get().findViewById(R.id.city)).setText(city);
//								((TextView)userInfoActivityWR.get().findViewById(R.id.country)).setText(country);
//							}
//						});
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, e.getMessage());
					}
					break;
				}
			}
		}

		private void registerNewUser(String unionID, String nickname, String avatarUrl) {
			RegisterRequest registerRequest = new RegisterRequest(md5(unionID), nickname, avatarUrl,"18565374389","401808209@qq.com","I am cute","Test1 Lily Jy Li");
			NFTApi apiService = NFTRetrofit.INSTANCE.getService();
			Observable observable = apiService.register(registerRequest);
			observable.compose(applySchedulers())
					.subscribe(new Observer<RegisterResponse>() {
						@Override
						public void onSubscribe(@NonNull Disposable d) {

						}

						@Override
						public void onNext(@NonNull RegisterResponse registerResponse) {
							Log.i(TAG, "success, registerResponse is "+registerResponse);
							wxEntryActivityWeakReference.get().gotoLogin(registerRequest.getUserId());
						}

						@Override
						public void onError(@NonNull Throwable e) {
							Log.i(TAG, "failed, "+e);
						}

						@Override
						public void onComplete() {

						}
					});
		}
	}

	private static String getcode (String str) {
		String[] encodelist ={"GB2312","ISO-8859-1","UTF-8","GBK","Big5","UTF-16LE","Shift_JIS","EUC-JP"};
		for(int i =0;i<encodelist.length;i++){
			try {
				if (str.equals(new String(str.getBytes(encodelist[i]),encodelist[i]))) {
					return encodelist[i];
				}
			} catch (Exception e) {

			} finally{

			}
		} return "";
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	api = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
		handler = new MyHandler(this);

        try {
            Intent intent = getIntent();
        	api.handleIntent(intent, this);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			goToGetMsg();		
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			goToShowMsg((ShowMessageFromWX.Req) req);
			break;
		default:
			break;
		}
        finish();
	}

	@Override
	public void onResp(BaseResp resp) {
		int result = 0;
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		case BaseResp.ErrCode.ERR_UNSUPPORT:
			result = R.string.errcode_unsupported;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}
		
		Toast.makeText(this, getString(result) + ", type=" + resp.getType(), Toast.LENGTH_SHORT).show();


		if (resp.getType() == ConstantsAPI.COMMAND_SUBSCRIBE_MESSAGE) {
			SubscribeMessage.Resp subscribeMsgResp = (SubscribeMessage.Resp) resp;
			String text = String.format("openid=%s\ntemplate_id=%s\nscene=%d\naction=%s\nreserved=%s",
					subscribeMsgResp.openId, subscribeMsgResp.templateID, subscribeMsgResp.scene, subscribeMsgResp.action, subscribeMsgResp.reserved);

			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}

        if (resp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            WXLaunchMiniProgram.Resp launchMiniProgramResp = (WXLaunchMiniProgram.Resp) resp;
            String text = String.format("openid=%s\nextMsg=%s\nerrStr=%s",
                    launchMiniProgramResp.openId, launchMiniProgramResp.extMsg,launchMiniProgramResp.errStr);

            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        if (resp.getType() == ConstantsAPI.COMMAND_OPEN_BUSINESS_VIEW) {
            WXOpenBusinessView.Resp launchMiniProgramResp = (WXOpenBusinessView.Resp) resp;
            String text = String.format("openid=%s\nextMsg=%s\nerrStr=%s\nbusinessType=%s",
                    launchMiniProgramResp.openId, launchMiniProgramResp.extMsg,launchMiniProgramResp.errStr,launchMiniProgramResp.businessType);

            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

        if (resp.getType() == ConstantsAPI.COMMAND_OPEN_BUSINESS_WEBVIEW) {
            WXOpenBusinessWebview.Resp response = (WXOpenBusinessWebview.Resp) resp;
            String text = String.format("businessType=%d\nresultInfo=%s\nret=%d",response.businessType,response.resultInfo,response.errCode);

            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }

		if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
			SendAuth.Resp authResp = (SendAuth.Resp)resp;
			final String code = authResp.code;
//			getTokenByCIAM(code);
			getTokenByWechat(code);
		}
        finish();
	}

	private void getTokenByCIAM(String code) {

	}

	private void getTokenByWechat(String code) {
		NetworkUtil.sendWxAPI(handler, String.format("https://api.weixin.qq.com/sns/oauth2/access_token?" +
						"appid=%s&secret=%s&code=%s&grant_type=authorization_code", WX_APP_ID,
				WX_APP_SECRET, code), NetworkUtil.GET_TOKEN);
	}

	private void gotoLogin(String unionID){
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(WX_UNION_ID, unionID);
		startActivity(intent);
		finish();
	}

	private void goToGetMsg() {
//		Intent intent = new Intent(this, GetFromWXActivity.class);
//		intent.putExtras(getIntent());
//		startActivity(intent);
//		finish();
	}
	
	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
//		WXMediaMessage wxMsg = showReq.message;
//		WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
//
//		StringBuffer msg = new StringBuffer();
//		msg.append("description: ");
//		msg.append(wxMsg.description);
//		msg.append("\n");
//		msg.append("extInfo: ");
//		msg.append(obj.extInfo);
//		msg.append("\n");
//		msg.append("filePath: ");
//		msg.append(obj.filePath);
//
//		Intent intent = new Intent(this, ShowFromWXActivity.class);
//		intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
//		intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
//		intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
//		startActivity(intent);
//		finish();
	}
}