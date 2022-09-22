/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { SubscriptionGroupListV1 } from "../../components/subscription-group-list-v1/subscription-group-list-v1.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import { useGetSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.actions";
import { deleteSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getSubscriptionGroupsCreatePath } from "../../utils/routes/routes.util";
import { getUiSubscriptionGroups } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsAllPage: FunctionComponent = () => {
    const {
        subscriptionGroups,
        getSubscriptionGroups,
        status: getSubscriptionGroupStatus,
        errorMessages: getSubscriptionGroupErrorMessages,
    } = useGetSubscriptionGroups();

    const {
        alerts,
        getAlerts,
        status: getAlertsStatus,
        errorMessages: getAlertsErrorMessages,
    } = useGetAlerts();

    const [uiSubscriptionGroups, setUiSubscriptionGroups] = useState<
        UiSubscriptionGroup[]
    >([]);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch subscription groups
        fetchAllSubscriptionGroups();
    }, []);

    useEffect(() => {
        notifyIfErrors(
            getSubscriptionGroupStatus,
            getSubscriptionGroupErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.subscription-groups"),
            })
        );
    }, [getSubscriptionGroupStatus]);

    useEffect(() => {
        notifyIfErrors(
            getAlertsStatus,
            getAlertsErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alerts"),
            })
        );
    }, [getAlertsStatus]);

    useEffect(() => {
        if (!alerts || !subscriptionGroups) {
            return;
        }
        setUiSubscriptionGroups(
            getUiSubscriptionGroups(subscriptionGroups, alerts)
        );
    }, [alerts, subscriptionGroups]);

    const fetchAllSubscriptionGroups = (): void => {
        setUiSubscriptionGroups([]);

        getSubscriptionGroups();
        getAlerts();
    };

    const handleSubscriptionGroupDelete = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiSubscriptionGroup.name,
            }),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleSubscriptionGroupDeleteOk(uiSubscriptionGroup),
        });
    };

    const handleSubscriptionGroupDeleteOk = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        deleteSubscriptionGroup(uiSubscriptionGroup.id).then(
            (subscriptionGroup) => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.subscription-group"),
                    })
                );

                // Remove deleted subscription group from fetched subscription groups
                removeUiSubscriptionGroup(subscriptionGroup);
            }
        );
    };

    const removeUiSubscriptionGroup = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        setUiSubscriptionGroups(
            (uiSubscriptionGroups) =>
                uiSubscriptionGroups &&
                uiSubscriptionGroups.filter(
                    (uiSubscriptionGroup) =>
                        uiSubscriptionGroup.id !== subscriptionGroup.id
                )
        );
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={4} />
            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    isError={
                        getSubscriptionGroupStatus === ActionStatus.Error ||
                        getAlertsStatus === ActionStatus.Error
                    }
                    isLoading={
                        getSubscriptionGroupStatus === ActionStatus.Working ||
                        getAlertsStatus === ActionStatus.Working
                    }
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <Box padding={20}>
                                        <NoDataIndicator>
                                            <Box textAlign="center">
                                                {t(
                                                    "message.no-entity-created",
                                                    {
                                                        entity: t(
                                                            "label.subscription-groups"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                            <Box
                                                marginTop={2}
                                                textAlign="center"
                                            >
                                                <Button
                                                    color="primary"
                                                    href={getSubscriptionGroupsCreatePath()}
                                                >
                                                    {t("label.create-entity", {
                                                        entity: t(
                                                            "label.subscription-group"
                                                        ),
                                                    })}
                                                </Button>
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={uiSubscriptionGroups.length === 0}
                    >
                        <SubscriptionGroupListV1
                            subscriptionGroups={uiSubscriptionGroups}
                            onDelete={handleSubscriptionGroupDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
