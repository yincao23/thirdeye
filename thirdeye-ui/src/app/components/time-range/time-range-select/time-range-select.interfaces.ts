import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectProps {
    fullwidth?: boolean;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedTimeRange?: TimeRange;
    onChange?: (eventObject: TimeRangeDuration | TimeRange) => void;
}