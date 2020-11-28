import axios from "axios";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ApplicationBar } from "./components/application-bar/application-bar.component";
import { ApplicationRouter } from "./routers/application-router";
import { useAuthStore } from "./store/auth/auth-store";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./utils/axios/axios-util";
import { SnackbarOption } from "./utils/snackbar/snackbar-util";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [axiosRequestInterceptorId, setAxiosRequestInterceptorId] = useState(
        0
    );
    const [
        axiosResponseInterceptorId,
        setAxiosResponseInterceptorId,
    ] = useState(0);
    const [accessToken, removeAccessToken] = useAuthStore((state) => [
        state.accessToken,
        state.removeAccessToken,
    ]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setLoading(true);

        // Axios initialization
        // Clear existing interceptors
        axios.interceptors.request.eject(axiosRequestInterceptorId);
        axios.interceptors.response.eject(axiosResponseInterceptorId);
        // Set new interceptors
        const requestInterceptorId = axios.interceptors.request.use(
            getRequestInterceptor(accessToken)
        );
        const responseInterceptorId = axios.interceptors.response.use(
            getFulfilledResponseInterceptor(),
            getRejectedResponseInterceptor(unauthenticatedAccessHandler)
        );
        setAxiosRequestInterceptorId(requestInterceptorId);
        setAxiosResponseInterceptorId(responseInterceptorId);

        setLoading(false);
    }, [accessToken]);

    const unauthenticatedAccessHandler = (): void => {
        // Notify
        enqueueSnackbar(t("message.signed-out"), SnackbarOption.ERROR);

        // Sign out
        removeAccessToken();
    };

    if (loading) {
        // Wait until initialization completes
        return <></>;
    }

    return (
        <>
            {/* Application bar */}
            <ApplicationBar />

            {/* Application router */}
            <ApplicationRouter />
        </>
    );
};
