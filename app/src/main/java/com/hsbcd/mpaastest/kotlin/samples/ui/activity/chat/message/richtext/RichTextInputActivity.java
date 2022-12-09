package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.richtext;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cn.com.hsbc.hsbcchina.cert.databinding.ActivityRichTextInputBinding;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * 富文本输入
 */
public class RichTextInputActivity extends AppCompatActivity {

    private ActivityRichTextInputBinding binding;

    private static final String DEFAULT_RICH_TEXT_CONTENT = "<html>\n" +
            "\n" +
            "<head>\n" +
            "    <h3>Rich Text</h3>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "    <div>\n" +
            "        <img src=\"https://gw.alipayobjects.com/mdn/rms_b33675/afts/img/A*mrv_SrL2D_EAAAAAAAAAAAAAARQnAQ\" width=\"256\" height=\"256\"></img>\n" +
            "    </div>\n" +
            "\n" +
            "    <div style=\"margin-top: 20px;\">\n" +
            "        <a href=\"http://ccmimplus.cloud.alipay.com/portal/index.htm#/implus\" style=\"color: blue;\">Click to access website</a>\n" +
            "    </div>\n" +
            "</body>\n" +
            "\n" +
            "</html>";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRichTextInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener((e) -> onBackPressed());
        binding.msgRichTextSend.setOnClickListener((e) -> sendRichTextMsg());
        binding.msgRichTextInput.setText(DEFAULT_RICH_TEXT_CONTENT);
    }

    private void sendRichTextMsg() {
        String text=binding.msgRichTextInput.getText().toString();
        if(StringUtils.isBlank(text)){
            ToastUtil.makeToast(this,"请输入富文本html内容",3000);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("text", text);

        setResult(RESULT_OK, intent);
        finish();
    }
}