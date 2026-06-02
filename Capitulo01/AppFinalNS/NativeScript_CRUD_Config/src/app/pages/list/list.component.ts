import { Component, OnInit, NO_ERRORS_SCHEMA } from "@angular/core";
import {
  RouterExtensions,
  NativeScriptCommonModule,
} from "@nativescript/angular";

import { confirm, alert } from "@nativescript/core";
import { ItemsService, Item } from "../../data/item.service";

@Component({
  selector: "ns-list",
  templateUrl: "./list.component.html",
  styleUrls: ["./list.component.css"],
  standalone: true,
  imports: [
    NativeScriptCommonModule
  ],
  schemas: [NO_ERRORS_SCHEMA]
})
export class ListComponent implements OnInit {
  items: Item[] = [];

  constructor(
    private itemsService: ItemsService,
    private router: RouterExtensions
  ) {}

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    this.items = this.itemsService.getItems();
  }

  onAdd(): void {
    this.router.navigate(["/form"]);
  }

  onEdit(item: Item): void {
    this.router.navigate(["/form", item.id]);
  }

  onDelete(item: Item): void {
    confirm({
      title: "Confirmar eliminación",
      message: `¿Eliminar ${item.title}?`,
      okButtonText: "Sí",
      cancelButtonText: "No"
    }).then(result => {
      if (result && item.id !== undefined) {
        this.itemsService.deleteItem(item.id);
        this.loadItems();
        alert("Item eliminado correctamente");
      }
    });
  }
}
