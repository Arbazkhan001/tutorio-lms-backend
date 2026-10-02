package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.handler.InvalidTokenException;
import com.affycloud.TutorioBackend.services.FCMService;
import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FCMServiceImplementation implements FCMService {

        public String sendNotificationToToken(String title, String body, String deviceToken) {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            try {
                return FirebaseMessaging.getInstance().send(message);
            }catch (FirebaseMessagingException e) {
                ErrorCode errorCode = e.getErrorCode();
                if (errorCode == ErrorCode.NOT_FOUND
                        || errorCode == ErrorCode.UNAUTHENTICATED
                        || errorCode == ErrorCode.PERMISSION_DENIED
                        || errorCode == ErrorCode.INVALID_ARGUMENT
                        || errorCode == ErrorCode.UNAVAILABLE) {
                    throw new InvalidTokenException("Device token is invalid or expired.");
                }
                throw new RuntimeException("Firebase error: " + e.getMessage(), e);
            }

        }
    }