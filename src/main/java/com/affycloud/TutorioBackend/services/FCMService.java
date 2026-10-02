package com.affycloud.TutorioBackend.services;

public interface FCMService {

    String sendNotificationToToken(String title, String body, String deviceToken);

}
