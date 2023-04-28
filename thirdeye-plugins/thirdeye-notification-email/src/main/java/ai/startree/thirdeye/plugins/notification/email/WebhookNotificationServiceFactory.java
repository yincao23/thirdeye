package ai.startree.thirdeye.plugins.notification.email;

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;

import java.util.Map;

public class WebhookNotificationServiceFactory  implements NotificationServiceFactory {
    @Override
    public String name() {
        return "webhook";
    }

    @Override
    public NotificationService build(Map<String, Object> params) {
        return new WebhookNotificationService();
    }
}
