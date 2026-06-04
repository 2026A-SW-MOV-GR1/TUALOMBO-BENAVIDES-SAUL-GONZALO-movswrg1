import { Observable, action } from '@nativescript/core';
import { IDataRepository, SqlRepository, NoSqlRepository } from './repository';

export class MainViewModel extends Observable {
    private sqlRepo: SqlRepository;
    private noSqlRepo: NoSqlRepository;
    private activeRepo: IDataRepository;

    public isSqlActive: boolean = true;
    public currentEngineText: string = "Conectado a: SQLite (Relacional)";
    public indicatorColor: string = "#4A90E2";
    public items: any[] = [];

    // Variables del Formulario
    public formTitle: string = "Nuevo Registro";
    public submitButtonText: string = "Guardar";
    public newItemName: string = "";
    public newItemCategory: string = "";

    private isEditing: boolean = false;
    private editingId: number | null = null;

    constructor() {
        super();
        this.sqlRepo = new SqlRepository();
        this.noSqlRepo = new NoSqlRepository();
        this.activeRepo = this.sqlRepo;

        this.loadItems();
        this.on(Observable.propertyChangeEvent, (data: any) => {
            if (data.propertyName === "isSqlActive") this.toggleEngine(data.value);
        });
    }

    private logTrace(level: string, message: string) {
        console.log(`[${level}] - ${new Date().toISOString()}: ${message}`);
    }

    private async toggleEngine(useSql: boolean) {
        this.activeRepo = useSql ? this.sqlRepo : this.noSqlRepo;
        this.set("currentEngineText", useSql ? "Conectado a: SQLite (Relacional)" : "Conectado a: JSON (NoSQL)");
        this.set("indicatorColor", useSql ? "#4A90E2" : "#9013FE"); // Azul corporativo vs Morado

        this.resetForm(); // Limpiamos el form al cambiar de motor para evitar mezclar datos
        await this.loadItems();
    }

    public async loadItems() {
        const data = await this.activeRepo.getItems();
        this.set("items", data);
    }

    public async onSave() {
        if (!this.newItemName || !this.newItemCategory) {
            alert("Completa todos los campos");
            return;
        }

        const item = { name: this.newItemName, category: this.newItemCategory };

        try {
            if (this.isEditing && this.editingId) {
                await this.activeRepo.updateItem({ ...item, id: this.editingId });
                this.logTrace("INFO", `Registro ACTUALIZADO en ${this.isSqlActive ? 'SQL' : 'NoSQL'}.`);
            } else {
                await this.activeRepo.saveItem(item);
                this.logTrace("INFO", `Registro INSERTADO en ${this.isSqlActive ? 'SQL' : 'NoSQL'}.`);
            }
            this.resetForm();
            await this.loadItems();
        } catch (error) {
            console.error("Error guardando datos:", error);
        }
    }

    // Despliega un menú nativo al tocar un elemento de la lista
    public async onItemTap(args: any) {
        const tappedItem = this.items[args.index];

        const result = await action({
            message: `Opciones para: ${tappedItem.name}`,
            cancelButtonText: "Cancelar",
            actions: ["Editar", "Eliminar"]
        });

        if (result === "Editar") {
            this.set("newItemName", tappedItem.name);
            this.set("newItemCategory", tappedItem.category);
            this.set("formTitle", "Editando Registro");
            this.set("submitButtonText", "Actualizar Cambios");
            this.isEditing = true;
            this.editingId = tappedItem.id;
        } else if (result === "Eliminar") {
            await this.activeRepo.deleteItem(tappedItem.id);
            this.logTrace("INFO", `Registro ELIMINADO en ${this.isSqlActive ? 'SQL' : 'NoSQL'}.`);
            await this.loadItems();
        }
    }

    public resetForm() {
        this.set("newItemName", "");
        this.set("newItemCategory", "");
        this.set("formTitle", "Nuevo Registro");
        this.set("submitButtonText", "Guardar");
        this.isEditing = false;
        this.editingId = null;
    }
}
