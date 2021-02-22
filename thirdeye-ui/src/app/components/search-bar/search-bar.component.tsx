import { IconButton, InputAdornment, TextField } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import SearchIcon from "@material-ui/icons/Search";
import { debounce } from "lodash";
import React, {
    ChangeEvent,
    FunctionComponent,
    useCallback,
    useEffect,
    useRef,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    getSearchFromQueryString,
    getSearchTextFromQueryString,
    setSearchInQueryString,
    setSearchTextInQueryString,
} from "../../utils/params/params.util";
import { SearchBarProps } from "./search-bar.interfaces";

const DELIMITER_SEARCH_WORDS = " ";
const DELAY_HANDLE_ON_CHANGE = 400;

export const SearchBar: FunctionComponent<SearchBarProps> = (
    props: SearchBarProps
) => {
    const [searchText, setSearchText] = useState("");
    const searchInputRef = useRef<HTMLInputElement>(null);
    const { t } = useTranslation();

    useEffect(() => {
        // Pick up search from query string if required
        if (!props.setSearchQueryString) {
            return;
        }

        // If search label matches search query string, set search text from query string
        if (props.searchLabel === getSearchFromQueryString()) {
            // Update search text and arrange to send event with a delay to allow user to notice
            // search
            updateSearchText(getSearchTextFromQueryString(), true);
        }
    }, []);

    const handleSearchInputChange = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        // Update search text and arrange to send event with a delay to account for a burst of
        // change events
        updateSearchText(event.currentTarget.value, true);
    };

    const handleSearchClear = (): void => {
        // Update search text and arrange to send event immediately
        updateSearchText("", false);
        // Set focus
        searchInputRef &&
            searchInputRef.current &&
            searchInputRef.current.focus();
    };

    const updateSearchText = (searchText: string, debounced: boolean): void => {
        setSearchText(searchText);

        // Split search text into words
        const searchWords = searchText
            ? searchText.trim().split(DELIMITER_SEARCH_WORDS)
            : [];

        // Depending on the flag, arrange to send change event immediately or with a delay
        debounced
            ? handleOnChangeDebounced(searchWords)
            : handleOnChange(searchWords);
    };

    const handleOnChange = (searchWords: string[]): void => {
        props.onChange && props.onChange(searchWords);

        // Set search and search text in query string
        if (props.setSearchQueryString) {
            setSearchInQueryString(
                props.searchLabel ? props.searchLabel : t("label.search")
            );
            setSearchTextInQueryString(
                searchWords.join(DELIMITER_SEARCH_WORDS)
            );
        }
    };

    const handleOnChangeDebounced = useCallback(
        debounce(handleOnChange, DELAY_HANDLE_ON_CHANGE),
        [props.onChange, props.setSearchQueryString, props.searchLabel]
    );

    return (
        <TextField
            fullWidth
            InputProps={{
                startAdornment: (
                    // Search icon
                    <InputAdornment position="start">
                        <SearchIcon />
                    </InputAdornment>
                ),
                endAdornment: (
                    <>
                        {/* Search status label */}
                        <InputAdornment position="end">
                            {props.searchStatusLabel}
                        </InputAdornment>

                        {/* Clear search button */}
                        <InputAdornment position="end">
                            <IconButton onClick={handleSearchClear}>
                                <CloseIcon />
                            </IconButton>
                        </InputAdornment>
                    </>
                ),
            }}
            autoFocus={props.autoFocus}
            inputRef={searchInputRef}
            label={props.searchLabel || t("label.search")}
            value={searchText}
            variant="outlined"
            onChange={handleSearchInputChange}
        />
    );
};
