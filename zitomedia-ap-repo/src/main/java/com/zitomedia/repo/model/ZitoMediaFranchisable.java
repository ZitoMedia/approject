package com.zitomedia.repo.model;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by drq on 6/14/15.
 */
public class ZitoMediaFranchisable implements NodeServicePolicies.OnUpdatePropertiesPolicy {
    private static final Logger logger = Logger.getLogger(ZitoMediaFranchisable.class);

    private static final Integer FIRST_NOTIFICATION = 36;
    private static final Integer SECOND_NOTIFICATION = 30;

    /**
     * Policy component
     */
    private PolicyComponent policyComponent;

    /**
     * Service registry
     */
    private ServiceRegistry serviceRegistry;

    /**
     * @return
     */
    public PolicyComponent getPolicyComponent() {
        return policyComponent;
    }

    /**
     * Set the policy component
     *
     * @param policyComponent policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    /**
     * @return
     */
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    /**
     * Set the Alfresco service registry
     *
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Init method. Binds model behaviours to policies.
     */
    public void init() {
        this.getPolicyComponent().bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ZitoMediaModel.ASPECT_ZITOMEDIA_FRANCHISABLE,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Start to execute custom onUpdateProperties behavior on node %s.", nodeRef));
        }

        NodeService nodeService = this.getServiceRegistry().getNodeService();

        if (!nodeService.exists(nodeRef)) {
            return;
        }

        Date expirationDate = (Date) after.get(ZitoMediaModel.PROP_ZITOMEDIA_EXPIRATION_DATE);
        if (expirationDate != null) {
            DateTime dateTime = new DateTime(expirationDate.getTime());
            Date firstNotificationDate = (Date) after.get(ZitoMediaModel.PROP_ZITOMEDIA_FIRST_NOTIFICATION_DATE);
            if (firstNotificationDate == null) {
                // 30-month notification
                Date calculatedFirstNotificationDate = dateTime.minusMonths(FIRST_NOTIFICATION).toDate();
                nodeService.setProperty(nodeRef, ZitoMediaModel.PROP_ZITOMEDIA_FIRST_NOTIFICATION_DATE, calculatedFirstNotificationDate);
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Set %s first notification date %s", nodeRef, calculatedFirstNotificationDate));
                }
            }
            Date secondNotificationDate = (Date) after.get(ZitoMediaModel.PROP_ZITOMEDIA_SECOND_NOTIFICATION_DATE);
            if (secondNotificationDate == null) {
                // 36-month notification
                Date calculatedSecondNotificationDate = dateTime.minusMonths(SECOND_NOTIFICATION).toDate();
                nodeService.setProperty(nodeRef, ZitoMediaModel.PROP_ZITOMEDIA_SECOND_NOTIFICATION_DATE, calculatedSecondNotificationDate );
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Set %s second notification date %s", nodeRef, calculatedSecondNotificationDate ));
                }
            }
        }
    }
}
