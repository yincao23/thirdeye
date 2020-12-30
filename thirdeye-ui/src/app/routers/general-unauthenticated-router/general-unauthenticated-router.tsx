import React, { FunctionComponent, useEffect, useState } from "react";
import { Redirect, Route, Switch, useLocation } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { SignInPage } from "../../pages/sign-in-page/sign-in-page.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import { useRedirectionPathStore } from "../../store/redirection-path-store/redirection-path-store";
import {
    AppRoute,
    createPathWithRecognizedQueryString,
    getSignInPath,
} from "../../utils/routes-util/routes-util";

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setAppSectionBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumbs,
    ]);
    const [removeAppToolbar] = useAppToolbarStore((state) => [
        state.removeAppToolbar,
    ]);
    const [setRedirectionPath] = useRedirectionPathStore((state) => [
        state.setRedirectionPath,
    ]);
    const location = useLocation();

    useEffect(() => {
        // Create app section breadcrumbs
        setAppSectionBreadcrumbs([]);

        // No app toolbar under this router
        removeAppToolbar();

        setupRedirectionPath();

        setLoading(false);
    }, []);

    const setupRedirectionPath = (): void => {
        // If location is anything other than the sign in/out path, store it to redirect the user
        // after authentication
        if (
            location.pathname === AppRoute.SIGN_IN ||
            location.pathname === AppRoute.SIGN_OUT
        ) {
            return;
        }

        setRedirectionPath(
            createPathWithRecognizedQueryString(location.pathname)
        );
    };

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <Switch>
            {/* Sign in path */}
            <Route exact component={SignInPage} path={AppRoute.SIGN_IN} />

            {/* No match found, redirect to sign in path */}
            <Redirect to={getSignInPath()} />
        </Switch>
    );
};