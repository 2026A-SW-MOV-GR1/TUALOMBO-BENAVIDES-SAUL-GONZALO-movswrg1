import { Injectable } from "@angular/core";

export interface Item {
  id?: number;
  title: string;
  subtitle: string;
  image: string;
}

@Injectable({ providedIn: "root" })
export class ItemsService {
  private items: Item[] = [
    {
      id: 1,
      title: "Item 1",
      subtitle: "Subtítulo 1",
      image: "https://placekitten.com/100/100"
    },
    {
      id: 2,
      title: "Item 2",
      subtitle: "Subtítulo 2",
      image: "https://placekitten.com/101/101"
    },
    {
      id: 3,
      title: "Item 3",
      subtitle: "Subtítulo 3",
      image: "https://placekitten.com/102/102"
    }
  ];

  getItems(): Item[] {
    return this.items;
  }

  addItem(item: Item): void {
    const maxId = this.items.length > 0
      ? Math.max(...this.items.map(i => i.id ?? 0))
      : 0;

    item.id = maxId + 1;
    this.items.push(item);
  }

  updateItem(updatedItem: Item): void {
    const index = this.items.findIndex(i => i.id === updatedItem.id);

    if (index >= 0) {
      this.items[index] = updatedItem;
    }
  }

  deleteItem(id: number): void {
    this.items = this.items.filter(i => i.id !== id);
  }
}
