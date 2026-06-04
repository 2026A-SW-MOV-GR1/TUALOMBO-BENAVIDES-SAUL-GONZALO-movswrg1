import { knownFolders, File } from '@nativescript/core';
const Sqlite = require('nativescript-sqlite');

export interface IDataRepository {
    saveItem(item: any): Promise<void>;
    getItems(): Promise<any[]>;
    updateItem(item: any): Promise<void>; // NUEVO
    deleteItem(id: number): Promise<void>; // NUEVO
}

// ---------------------------------------------------------
// MOTOR A: SQL RELACIONAL
// ---------------------------------------------------------
export class SqlRepository implements IDataRepository {
    private dbPromise: Promise<any>;

    constructor() {
        this.dbPromise = new Sqlite("mi_base_relacional.db").then(db => {
            db.execSQL("CREATE TABLE IF NOT EXISTS items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT)");
            return db;
        });
    }

    async saveItem(item: any): Promise<void> {
        const db = await this.dbPromise;
        await db.execSQL("INSERT INTO items (name, category) VALUES (?, ?)", [item.name, item.category]);
    }

    async getItems(): Promise<any[]> {
        const db = await this.dbPromise;
        const result = await db.all("SELECT * FROM items ORDER BY id DESC");
        return result.map(row => ({ id: row[0], name: row[1], category: row[2] }));
    }

    async updateItem(item: any): Promise<void> {
        const db = await this.dbPromise;
        await db.execSQL("UPDATE items SET name = ?, category = ? WHERE id = ?", [item.name, item.category, item.id]);
    }

    async deleteItem(id: number): Promise<void> {
        const db = await this.dbPromise;
        await db.execSQL("DELETE FROM items WHERE id = ?", [id]);
    }
}

// ---------------------------------------------------------
// MOTOR B: NoSQL (Archivos Dinámicos JSON)
// ---------------------------------------------------------
export class NoSqlRepository implements IDataRepository {
    private getFile(): File {
        return knownFolders.documents().getFile("coleccion_nosql.json");
    }

    async saveItem(item: any): Promise<void> {
        const file = this.getFile();
        let data = await this.getItems();
        item.id = new Date().getTime();
        data.unshift(item); // Insertamos al inicio para emular el ORDER BY DESC
        await file.writeText(JSON.stringify(data));
    }

    async getItems(): Promise<any[]> {
        const file = this.getFile();
        try {
            const content = await file.readText();
            return content ? JSON.parse(content) : [];
        } catch (error) { return []; }
    }

    async updateItem(item: any): Promise<void> {
        const file = this.getFile();
        let data = await this.getItems();
        const index = data.findIndex(x => x.id === item.id);
        if (index > -1) {
            data[index] = item;
            await file.writeText(JSON.stringify(data));
        }
    }

    async deleteItem(id: number): Promise<void> {
        const file = this.getFile();
        let data = await this.getItems();
        data = data.filter(x => x.id !== id);
        await file.writeText(JSON.stringify(data));
    }
}
