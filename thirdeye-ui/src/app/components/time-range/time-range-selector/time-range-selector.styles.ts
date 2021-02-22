import { makeStyles } from "@material-ui/core";

const HEIGHT_TIME_RANGE_SELECTOR_MD_UP = 427;

export const useTimeRangeSelectorStyles = makeStyles((theme) => ({
    timeRangeSelectorButton: {
        minWidth: 0,
        padding: theme.spacing(1),
    },
    timeRangeSelectorContents: {
        [theme.breakpoints.only("xs")]: {
            width: 342,
        },
        [theme.breakpoints.only("sm")]: {
            width: 465,
        },
        [theme.breakpoints.up("sm")]: {
            padding: 0,
        },
        [theme.breakpoints.up("md")]: {
            height: HEIGHT_TIME_RANGE_SELECTOR_MD_UP,
            width: 828,
        },
        "&:last-child": {
            [theme.breakpoints.up("sm")]: {
                paddingBottom: 0,
            },
        },
    },
    timeRangeList: {
        [theme.breakpoints.only("sm")]: {
            height: "100%",
        },
        [theme.breakpoints.up("md")]: {
            height: HEIGHT_TIME_RANGE_SELECTOR_MD_UP,
            overflow: "auto",
        },
    },
    startTimeCalendarContainer: {
        [theme.breakpoints.up("md")]: {
            paddingRight: "0px !important",
        },
    },
    endTimeCalendarContainer: {
        [theme.breakpoints.up("md")]: {
            paddingLeft: "0px !important",
        },
    },
    startTimeCalendarLabel: {
        [theme.breakpoints.up("sm")]: {
            marginTop: 12,
            marginLeft: 12,
        },
    },
    endTimeCalendarLabel: {
        [theme.breakpoints.up("sm")]: {
            marginLeft: 12,
        },
        [theme.breakpoints.up("md")]: {
            marginTop: 12,
        },
    },
    calendar: {
        width: 310,
    },
}));
