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
import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { AlertTemplateListV1 } from "../../components/alert-template-list-v1/alert-template-list-v1.component";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
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
import { useGetAlertTemplates } from "../../rest/alert-templates/alert-templates.actions";
import {
    deleteAlertTemplate,
    updateAlertTemplate,
} from "../../rest/alert-templates/alert-templates.rest";
import { AlertTemplate } from "../../rest/dto/alert-template.interfaces";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertTemplatesCreatePath } from "../../utils/routes/routes.util";

export const AlertTemplatesAllPage: FunctionComponent = () => {
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const {
        alertTemplates,
        getAlertTemplates,
        status: alertTemplatesRequestStatus,
        errorMessages,
    } = useGetAlertTemplates();

    useEffect(() => {
        getAlertTemplates();
    }, []);

    const handleAlertTemplateChange = (
        alertTemplateToChange: AlertTemplate
    ): void => {
        if (!alertTemplateToChange) {
            return;
        }

        updateAlertTemplate(alertTemplateToChange).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.alert-template"),
                })
            );

            // Refresh list
            getAlertTemplates();
        });
    };

    const handleAlertTemplateDelete = (alertTemplate: AlertTemplate): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: alertTemplate.name,
            }),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleAlertTemplateDeleteOk(alertTemplate),
        });
    };

    const handleAlertTemplateDeleteOk = (
        alertTemplate: AlertTemplate
    ): void => {
        deleteAlertTemplate(alertTemplate.id)
            .then(() => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.alert-template"),
                    })
                );

                // Refresh list
                getAlertTemplates();
            })
            .catch((errors) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(errors as AxiosError),
                    notify,
                    t("message.delete-error", {
                        entity: t("label.alert-template"),
                    })
                );
            });
    };

    useEffect(() => {
        notifyIfErrors(
            alertTemplatesRequestStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert-templates"),
            })
        );
    }, [alertTemplatesRequestStatus, errorMessages]);

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={3} />

            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    isError={alertTemplatesRequestStatus == ActionStatus.Error}
                    isLoading={
                        alertTemplatesRequestStatus == ActionStatus.Working
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
                                                            "label.alert-templates"
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
                                                    href={getAlertTemplatesCreatePath()}
                                                >
                                                    {t("label.create-entity", {
                                                        entity: t(
                                                            "label.alert-template"
                                                        ),
                                                    })}
                                                </Button>
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={
                            !!alertTemplates && alertTemplates.length === 0
                        }
                    >
                        <AlertTemplateListV1
                            alertTemplates={alertTemplates}
                            onChange={handleAlertTemplateChange}
                            onDelete={handleAlertTemplateDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
