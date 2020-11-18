import { Button, Grid } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { login } from "../../rest/auth/auth.rest";
import { Auth } from "../../rest/dto/auth.interfaces";
import { setAccessToken } from "../../utils/auth/auth.util";
import { signInPageStyles } from "./sign-in-page.styles";

export const SignInPage: FunctionComponent = () => {
    const signInPageClasses = signInPageStyles();

    const { t } = useTranslation();

    const performLogin = async (): Promise<void> => {
        const authentication: Auth = await login();
        setAccessToken(authentication.accessToken);

        location.reload();
    };

    return (
        <PageContainer>
            <Grid
                container
                alignItems="center"
                className={signInPageClasses.grid}
                justify="center"
            >
                <Grid item>
                    <Button
                        color="primary"
                        variant="contained"
                        onClick={performLogin}
                    >
                        {t("label.sign-in")}
                    </Button>
                </Grid>
            </Grid>
        </PageContainer>
    );
};
