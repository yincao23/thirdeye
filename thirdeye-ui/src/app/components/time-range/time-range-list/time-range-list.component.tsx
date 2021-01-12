import { List, ListItem, ListItemText, Tooltip } from "@material-ui/core";
import classnames from "classnames";
import { isEmpty } from "lodash";
import React, { Fragment, FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    formatTimeRange,
    formatTimeRangeDuration,
} from "../../../utils/time-range-util/time-range-util";
import { TimeRange } from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeListProps } from "./time-range-list.interfaces";
import { useTimeRangeListStyles } from "./time-range-list.styles";

export const TimeRangeList: FunctionComponent<TimeRangeListProps> = (
    props: TimeRangeListProps
) => {
    const timeRangeListClasses = useTimeRangeListStyles();
    const { t } = useTranslation();

    return (
        <List dense>
            {/* Recent custom time range durations label */}
            {!isEmpty(props.recentCustomTimeRangeDurations) && (
                <ListItem className={timeRangeListClasses.listLabel}>
                    <ListItemText
                        primary={t("label.recent-custom")}
                        primaryTypographyProps={{
                            variant: "overline",
                        }}
                    />
                </ListItem>
            )}

            {/* Recent custom time range durations */}
            {props.recentCustomTimeRangeDurations &&
                props.recentCustomTimeRangeDurations.map(
                    (recentCustomTimeRangeDuration, index) => (
                        <Tooltip
                            arrow
                            key={index}
                            placement="right"
                            title={formatTimeRangeDuration(
                                recentCustomTimeRangeDuration
                            )}
                        >
                            <ListItem
                                button
                                divider={
                                    (props.recentCustomTimeRangeDurations &&
                                        props.recentCustomTimeRangeDurations
                                            .length - 1) === index
                                }
                                onClick={(): void => {
                                    props.onClick &&
                                        props.onClick(
                                            recentCustomTimeRangeDuration
                                        );
                                }}
                            >
                                <ListItemText
                                    primary={formatTimeRangeDuration(
                                        recentCustomTimeRangeDuration
                                    )}
                                    primaryTypographyProps={{
                                        variant: "button",
                                        color: "primary",
                                        className:
                                            timeRangeListClasses.listItem,
                                    }}
                                />
                            </ListItem>
                        </Tooltip>
                    )
                )}

            {/* Time ranges */}
            {Object.values(TimeRange)
                .filter((timeRange) => typeof timeRange === "string")
                .map((timeRange, index) => (
                    <Fragment key={index}>
                        <ListItem
                            button
                            divider={
                                timeRange === TimeRange.CUSTOM ||
                                timeRange === TimeRange.LAST_30_DAYS ||
                                timeRange === TimeRange.YESTERDAY ||
                                timeRange === TimeRange.LAST_WEEK ||
                                timeRange === TimeRange.LAST_MONTH
                            }
                            onClick={(): void => {
                                props.onClick && props.onClick(timeRange);
                            }}
                        >
                            <ListItemText
                                primary={formatTimeRange(timeRange)}
                                primaryTypographyProps={{
                                    variant: "button",
                                    color: "primary",
                                    className:
                                        props.selectedTimeRange === timeRange
                                            ? classnames(
                                                  timeRangeListClasses.selectedListItem,
                                                  timeRangeListClasses.listItem
                                              )
                                            : timeRangeListClasses.listItem,
                                }}
                            />
                        </ListItem>
                    </Fragment>
                ))}
        </List>
    );
};