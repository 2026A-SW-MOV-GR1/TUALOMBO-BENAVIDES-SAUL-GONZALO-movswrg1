import { Component, OnInit, NO_ERRORS_SCHEMA } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import {
  RouterExtensions,
  NativeScriptCommonModule,
  NativeScriptFormsModule,
} from "@nativescript/angular";

import { ItemsService, Item } from "../../data/item.service";

@Component({
  selector: "ns-form",
  templateUrl: "./form.component.html",
  styleUrls: ["./form.component.css"],
  standalone: true,
  imports: [
    NativeScriptCommonModule,
    NativeScriptFormsModule
  ],
  schemas: [NO_ERRORS_SCHEMA]
})
export class FormComponent implements OnInit {
  item: Item = {
    title: "",
    subtitle: "",
    image: ""
  };

  constructor(
    private itemsService: ItemsService,
    private router: RouterExtensions,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params["id"]);

    if (id) {
      const found = this.itemsService.getItems().find(i => i.id === id);

      if (found) {
        this.item = { ...found };
      }
    }
  }

  save(): void {
    if (!this.item.title.trim()) {
      return;
    }

    if (this.item.id) {
      this.itemsService.updateItem(this.item);
    } else {
      this.itemsService.addItem(this.item);
    }

    this.router.back();
  }

  cancel(): void {
    this.router.back();
  }
}
