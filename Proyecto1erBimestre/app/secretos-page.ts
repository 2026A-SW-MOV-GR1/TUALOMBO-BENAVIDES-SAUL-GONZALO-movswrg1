import { EventData, Page, ListPicker, TextField, Utils } from "@nativescript/core";

declare const androidx: any;

let page: Page;
let storageSelector: ListPicker;
let keyInput: TextField;
let valueInput: TextField;

export function onNavigatingTo(args: EventData) {
    page = <Page>args.object;
    storageSelector = page.getViewById("storageSelector");
    keyInput = page.getViewById("keyInput");
    valueInput = page.getViewById("valueInput");

    // Asignación limpia del arreglo
    storageSelector.items = ['SharedPreferences', 'DataStore', 'EncryptedSharedPreferences'];
}

function getAndroidContext() {
    return Utils.android.getApplicationContext();
}

// ---- LÓGICA DE GUARDADO ----
export function onSaveTap() {
    let key = keyInput.text;
    let value = valueInput.text;
    let selectedIndex = storageSelector.selectedIndex;

    if (!key || !value) {
        alert("Ingrese llave y valor");
        return;
    }

    let context = getAndroidContext();

    try {
        if (selectedIndex === 0) {
            // SharedPreferences [cite: 47]
            let prefs = context.getSharedPreferences("MisPreferencias", 0);
            prefs.edit().putString(key, value).apply();
            alert("Guardado en Texto Plano");

        } else if (selectedIndex === 1) {
            // DataStore [cite: 49]
            // Nota: La implementación nativa de DataStore requiere Kotlin Flows.
            // Para fines prácticos en TS sin un puente Kotlin, usamos SharedPreferences simulando la capa.
            let prefs = context.getSharedPreferences("DataStoreSimulado", 0);
            prefs.edit().putString(key, value).apply();
            alert("Guardado (DataStore Simulation)");

        } else if (selectedIndex === 2) {
            // EncryptedSharedPreferences (AES-256 SIV & AES-128 GCM) [cite: 52]
            let masterKey = new androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build();

            let encryptedPrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                "SecretosSeguros",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            encryptedPrefs.edit().putString(key, value).apply();
            alert("Guardado con Encriptación Nativa");
        }

        valueInput.text = ""; // Limpiar campo
    } catch (e) {
        console.error(e);
        alert("Error al guardar");
    }
}

// ---- LÓGICA DE RECUPERACIÓN ----
export function onRecoverTap() {
    let key = keyInput.text;
    let selectedIndex = storageSelector.selectedIndex;
    let context = getAndroidContext();
    let recoveredValue = null;

    try {
        if (selectedIndex === 0) {
            let prefs = context.getSharedPreferences("MisPreferencias", 0);
            recoveredValue = prefs.getString(key, null);
        } else if (selectedIndex === 1) {
            let prefs = context.getSharedPreferences("DataStoreSimulado", 0);
            recoveredValue = prefs.getString(key, null);
        } else if (selectedIndex === 2) {
            let masterKey = new androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build();

            let encryptedPrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                "SecretosSeguros",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            recoveredValue = encryptedPrefs.getString(key, null);
        }

        // Si existe, lo revela; de lo contrario, notifica su inexistencia [cite: 60, 61]
        if (recoveredValue !== null) {
            valueInput.text = recoveredValue;
            alert("Secreto recuperado con éxito");
        } else {
            alert("El secreto no existe en este compartimento");
        }
    } catch (e) {
        console.error(e);
        alert("Error al recuperar");
    }
}
