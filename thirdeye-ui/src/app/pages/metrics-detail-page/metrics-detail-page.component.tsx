import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { MetricCard } from "../../components/entity-cards/metric-card/metric-card.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deleteMetric, getMetric } from "../../rest/metrics/metrics.rest";
import {
    createEmptyUiMetric,
    getUiMetric,
} from "../../utils/metrics/metrics.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getMetricsAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { MetricsDetailPageParams } from "./metrics-detail-page.interfaces";

export const MetricsDetailPage: FunctionComponent = () => {
    const [uiMetric, setUiMetric] = useState<UiMetric | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<MetricsDetailPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchMetric();
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch metric
        fetchMetric();
    }, [timeRangeDuration]);

    const fetchMetric = (): void => {
        setUiMetric(null);
        let fetchedUiMetric = createEmptyUiMetric();

        if (!isValidNumberId(params.id)) {
            // Invalid id
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.metric"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            setUiMetric(fetchedUiMetric);

            return;
        }

        getMetric(toNumber(params.id))
            .then((metric) => {
                fetchedUiMetric = getUiMetric(metric);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                )
            )
            .finally(() => setUiMetric(fetchedUiMetric));
    };

    const handleMetricDelete = (uiMetric: UiMetric): void => {
        if (!uiMetric) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiMetric.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleMetricDeleteOk(uiMetric),
        });
    };

    const handleMetricDeleteOk = (uiMetric: UiMetric): void => {
        if (!uiMetric) {
            return;
        }

        deleteMetric(uiMetric.id)
            .then(() => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.metric") }),
                    getSuccessSnackbarOption()
                );

                // Redirect to metrics all path
                history.push(getMetricsAllPath());
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.metric") }),
                    getErrorSnackbarOption()
                )
            );
    };

    return (
        <PageContents
            centered
            hideTimeRange
            title={uiMetric ? uiMetric.name : ""}
        >
            <MetricCard metric={uiMetric} onDelete={handleMetricDelete} />
        </PageContents>
    );
};
