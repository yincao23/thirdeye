import { Box } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { CustomCellProps } from "./custom-cell.interfaces";

export const CustomCell: FunctionComponent<CustomCellProps> = (
    props: CustomCellProps
) => {
    const [align, setAlign] = useState("");

    useEffect(() => {
        setAlign(
            props.params && props.params.colDef && props.params.colDef.align
        );
    }, []);

    return (
        <Box textAlign={align} width="100%">
            {props.children}
        </Box>
    );
};
