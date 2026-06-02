import { EventData, Button } from "@nativescript/core";

export function onGoToRest(args: EventData) {
    // 1. Capturamos el botón que fue presionado
    let button = <Button>args.object;
    // 2. Navegamos usando el Frame exacto de esa pantalla
    button.page.frame.navigate("rest-page");
}

export function onGoToSecretos(args: EventData) {
    let button = <Button>args.object;
    button.page.frame.navigate("secretos-page");
}
