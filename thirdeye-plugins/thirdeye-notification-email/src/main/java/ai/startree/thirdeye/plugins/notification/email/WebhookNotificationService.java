package ai.startree.thirdeye.plugins.notification.email;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WebhookNotificationService implements NotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(WebhookNotificationService.class);

    @Override
    public void notify(NotificationPayloadApi api) throws ThirdEyeException {
        LOG.info("webhook success");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Y2Q0YWQ1OGEtYTY2NS00ZjU0LWIzNGItODY3YzY5NjdlYTFkZmVhZThiMjAtZDcx_PF84_1eb65fdf-9643-417f-9974-ad72cae0e10f");
        Gson gson = new Gson();
        String metrics = gson.toJson(api.getReport());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roomId", "Y2lzY29zcGFyazovL3VzL1JPT00vMDFjZWRiZTAtZTNmNS0xMWVkLTkzNzQtNzlkNTc0ZTI1OTY3");
        jsonObject.put("markdown", metrics);
        try {
            HttpResponse<JsonNode> jsonNodeHttpResponse = Unirest.post("")
                    .headers(headers)
                    .body(jsonObject.toString())
                    .asJson();
        } catch (Exception e) {
            LOG.info("NotificationPayloadApi {}", api.toString());

            LOG.error("webhook error", e);
        }


    }
}
