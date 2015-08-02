package com.zitomedia.repo.model;

import org.alfresco.service.namespace.QName;

/**
 * Created by drq on 6/17/15.
 */
public class ZitoMediaModel {
    public static final String ZITOMEDIA_URI = "http://www.zitomedia.com/model/content/1.0";

    // Types
    public static final QName TYPE_ZITOMEDIA_FRANCHISE_AGREEMENT = QName.createQName(ZITOMEDIA_URI, "franchiseAgreement");

    // Aspects
    public static final QName ASPECT_ZITOMEDIA_FRANCHISABLE = QName.createQName(ZITOMEDIA_URI, "franchisable");

    // Properties
    public static final QName PROP_ZITOMEDIA_EXPIRATION_DATE = QName.createQName(ZITOMEDIA_URI, "expirationDate");
    public static final QName PROP_ZITOMEDIA_FIRST_NOTIFICATION_DATE = QName.createQName(ZITOMEDIA_URI, "firstNotificationDate");
    public static final QName PROP_ZITOMEDIA_SECOND_NOTIFICATION_DATE = QName.createQName(ZITOMEDIA_URI, "secondNotificationDate");
}
