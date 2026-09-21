package com.midland.saloon.Notification.Controller;

import com.midland.saloon.Notification.Model.Notification;
import com.midland.saloon.Notification.Service.NotificationService;
import com.midland.saloon.Utils.Responses.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/findMyNotifications")
    public Response<List<Notification>> findMyNotifications() {
        return new Response<>(notificationService.findMyNotifications());
    }

    @GetMapping("/countUnread")
    public Response<Long> countUnread() {
        return new Response<>(notificationService.countUnread());
    }

    @PostMapping("/markAsRead/{notificationUID}")
    public Response<Boolean> markAsRead(@PathVariable String notificationUID) {
        notificationService.markAsRead(notificationUID);
        // Response(T data) — not Response(String message), which would
        // silently mark this SUCCESS as a WARNING and pop an unwanted
        // "ok" dialog via StatusInterceptor's generic message handling.
        return new Response<>(true);
    }

    @PostMapping("/markAllAsRead")
    public Response<Boolean> markAllAsRead() {
        notificationService.markAllAsRead();
        return new Response<>(true);
    }
}
