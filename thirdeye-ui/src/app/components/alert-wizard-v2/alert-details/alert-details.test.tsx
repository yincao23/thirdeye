import { fireEvent, render, screen } from "@testing-library/react";
import React from "react";
import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import { AlertDetails } from "./alert-details.component";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("AlertDetails", () => {
    it("should render name and description from passed alert", async () => {
        render(
            <AlertDetails<EditableAlert>
                alert={MOCK_ALERT}
                onAlertPropertyChange={() => {
                    return;
                }}
            />
        );

        expect(screen.getByDisplayValue("hello-world")).toBeInTheDocument();
        expect(screen.getByDisplayValue("foo bar")).toBeInTheDocument();
    });

    it("should render error state if name is empty and should not have called callback", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertDetails<EditableAlert>
                alert={MOCK_ALERT}
                onAlertPropertyChange={mockCallback}
            />
        );

        // Verify valid state
        const nameInput = screen.getByDisplayValue("hello-world");

        expect(nameInput).toBeInTheDocument();
        expect(screen.getByDisplayValue("foo bar")).toBeInTheDocument();

        // Enter empty name
        fireEvent.change(nameInput, { target: { value: "" } });

        expect(screen.getByTestId("name-input-label")).toHaveClass("Mui-error");

        expect(screen.getByText("message.cannot-be-empty")).toBeInTheDocument();
        expect(mockCallback).toHaveBeenCalledTimes(0);
    });

    it("should have called callback if valid input for name and description", async () => {
        const mockCallback = jest.fn();
        render(
            <AlertDetails<EditableAlert>
                alert={MOCK_ALERT}
                onAlertPropertyChange={mockCallback}
            />
        );

        // Verify name
        const nameInput = screen.getByDisplayValue("hello-world");

        fireEvent.change(nameInput, { target: { value: "new-value" } });

        expect(mockCallback).toHaveBeenCalledWith({ name: "new-value" });

        // Verify description
        const descriptionInput = screen.getByDisplayValue("foo bar");

        fireEvent.change(descriptionInput, {
            target: { value: "new-value-123" },
        });

        expect(mockCallback).toHaveBeenCalledWith({
            description: "new-value-123",
        });
    });
});

const MOCK_ALERT: EditableAlert = {
    name: "hello-world",
    description: "foo bar",
    cron: "",
    templateProperties: {},
};