import { Routes } from "@angular/router";
import { ListComponent } from "./pages/list/list.component";
import { FormComponent } from "./pages/form/form.component";

export const routes: Routes = [
  { path: "", redirectTo: "list", pathMatch: "full" },
  { path: "list", component: ListComponent },
  { path: "form", component: FormComponent },
  { path: "form/:id", component: FormComponent },
];
