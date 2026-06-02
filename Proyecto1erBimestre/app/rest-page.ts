import { EventData, Page, TextField, Button, TextView, ActivityIndicator } from "@nativescript/core";

// Variables globales de la interfaz
let page: Page;
let idInput: TextField;
let jsonOutput: TextView;
let btnGet: Button;
let btnPut: Button;
let loader: ActivityIndicator;

// NUEVO: Nuestra memoria temporal (Caché Optimista)
let localCache: { [key: string]: any } = {};

export function onNavigatingTo(args: EventData) {
    page = <Page>args.object;
    idInput = page.getViewById("idInput");
    jsonOutput = page.getViewById("jsonOutput");
    btnGet = page.getViewById("btnGet");
    btnPut = page.getViewById("btnPut");
    loader = page.getViewById("loader");
}

// Función para bloquear/desbloquear la interfaz
function setUiState(isLoading: boolean) {
    let isEnabled = !isLoading;
    idInput.isEnabled = isEnabled;
    jsonOutput.isEnabled = isEnabled;
    btnGet.isEnabled = isEnabled;
    btnPut.isEnabled = isEnabled;

    loader.busy = isLoading;
    loader.visibility = isLoading ? "visible" : "collapse";
}

// ---- LÓGICA DE CONSULTA (GET) ----
export async function onGetTap() {
    let postId = idInput.text;
    if (!postId) {
        alert("Por favor ingrese un ID válido");
        return;
    }

    setUiState(true);

    try {
        // Revisamos si el post ya fue editado y está en nuestra memoria
        if (localCache[postId]) {
            jsonOutput.text = JSON.stringify(localCache[postId], null, 2);
            console.log("Cargado desde el caché local");
        } else {
            // Si no está en memoria, hacemos la petición normal a JSONPlaceholder
            let response = await fetch(`https://jsonplaceholder.typicode.com/posts/${postId}`);
            let data = await response.json();
            jsonOutput.text = JSON.stringify(data, null, 2);
            console.log("Cargado desde la red");
        }
    } catch (error) {
        console.error("Error en GET:", error);
        alert("Error al conectar con el servidor.");
    } finally {
        setUiState(false);
    }
}

// ---- LÓGICA DE ACTUALIZACIÓN (PUT) ----
export async function onPutTap() {
    let postId = idInput.text;
    let updatedJson = jsonOutput.text;

    if (!postId || !updatedJson) {
        alert("Consulte un post primero antes de actualizar");
        return;
    }

    setUiState(true);

    try {
        // Enviamos la petición de actualización al servidor
        let response = await fetch(`https://jsonplaceholder.typicode.com/posts/${postId}`, {
            method: 'PUT',
            body: updatedJson,
            headers: { 'Content-type': 'application/json; charset=UTF-8' }
        });

        // Confirmación: Capturar código 200 OK
        if(response.status === 200) {
            // Parseamos el texto modificado y lo guardamos en nuestro caché con el ID correspondiente
            localCache[postId] = JSON.parse(updatedJson);

            alert("¡Actualización exitosa (Guardada en caché)!");
        }
    } catch (error) {
        console.error("Error en PUT:", error);
        alert("Error al actualizar. Asegúrate de no romper el formato JSON.");
    } finally {
        setUiState(false);
    }
}
