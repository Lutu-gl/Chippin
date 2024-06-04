import {Component, OnInit} from "@angular/core";

import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {NgForm, NgModel} from "@angular/forms";
import {Observable} from "rxjs";
import {RecipeCreateWithoutUserDto} from "../../../dtos/recipe";
import {ItemCreateDto, Unit} from "../../../dtos/item";
import {RecipeService} from "../../../services/recipe.service";
import {clone} from "lodash";
import {ConfirmationService, MessageService} from "primeng/api";



@Component({
  selector: 'app-recipe-create',
  templateUrl: './recipe-create.component.html',
  styleUrl: './recipe-create.component.scss'
})

export class RecipeCreateComponent implements OnInit {
  error = false;
  errorMessage = '';
  recipe: RecipeCreateWithoutUserDto = {
    name: '',
    ingredients: [],
    description: '',
    isPublic: false,
    portionSize:1
  };
  newIngredient: ItemCreateDto = {
    amount: 0,
    unit: Unit.Piece,
    description: ""
  };
  itemToEdit: ItemCreateDto = undefined;
  submitted: boolean = false;
  newItemDialog: boolean = false;
  itemDialog: boolean = false;

  selectedItems!: ItemCreateDto[] | null;


  constructor(
    private service: RecipeService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private notification: ToastrService,
  ) {
  }



  ngOnInit(): void {
  }


  public onRecipeSubmit(form: NgForm): void {
    if (form.valid) {
      let observable: Observable<RecipeCreateWithoutUserDto>;

          observable = this.service.createRecipe(this.recipe);

      observable.subscribe({
        next: data => {
          this.messageService.add({severity: 'success', summary: 'Success', detail: `Recipe ${this.recipe.name} successfully created.`});

          this.router.navigate(['/recipe']);
        },
        error: error => {
          this.printError(error);
        }
      });
    }
  }

  onIngredientSubmit() {

      this.recipe.ingredients.push(this.newIngredient);
      this.newIngredient= {
        amount: 0,
        unit: Unit.Piece,
        description: ""}

    this.hideDialog();


  }


  hideDialog() {
    this.itemDialog = false;
    this.newItemDialog = false;
    this.submitted = false;
  }


  handleEnter(event: KeyboardEvent): void {
    event.preventDefault(); // Prevents the default behavior of adding a new line in the textarea
    this.recipe.description += '\n';
  }


  printError(error): void {
    if (error && error.error && error.error.errors) {
      for (let i = 0; i < error.error.errors.length; i++) {
        this.messageService.add({severity: 'error', summary: 'Error', detail: `${error.error.errors[i]}`});
      }
    } else if (error && error.error && error.error.message) {
        this.messageService.add({severity: 'error', summary: 'Error', detail: `${error.error.message}`});
    } else if (error && error.error && error.error.detail) {
        this.messageService.add({severity: 'error', summary: 'Error', detail: `${error.error.detail}`});
    } else {
      console.error('Could not load pantry items', error);
      this.messageService.add({severity: 'error', summary: 'Error', detail: `Could not load pantry!`});
    }
  }

  deleteSelectedItems(index: number) {
    this.confirmationService.confirm({
      message: 'Are you sure you want to remove ' + this.recipe.ingredients[index].description + '?',
      header: 'Confirm',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.recipe.ingredients.splice(index, 1);
      }
    });
  }

  decrement(index: number) {
    this.recipe.ingredients[index].amount -= this.getRecipeStepSize(this.recipe.ingredients[index]);
    if(this.recipe.ingredients[index].amount < 0) {
      this.recipe.ingredients[index].amount = 0;
    }

  }

  increment(index: number) {
    this.recipe.ingredients[index].amount += this.getRecipeStepSize(this.recipe.ingredients[index]);
    if(this.recipe.ingredients[index].amount > 1000000) {
      this.recipe.ingredients[index].amount = 1000000;
    }
  }

  openNew() {
    this.newIngredient = {
      description: "",
      amount: 0,
      unit: Unit.Piece,
    };
    this.submitted = false;
    this.newItemDialog = true;
  }
  getRecipeStepSize(item: ItemCreateDto): number {
    switch (item.unit) {
      case Unit.Piece:
        return item.amount > 100 ? 10 : 1;
      case Unit.Gram:
        switch (true) {
          case (item.amount < 100):
            return 10;
          case (item.amount < 1000):
            return 100;
          case (item.amount < 10000):
            return 250;
          default:
            return 1000;
        }
      case Unit.Milliliter:
        switch (true) {
          case (item.amount < 100):
            return 10;
          case (item.amount < 1000):
            return 100;
          case (item.amount < 10000):
            return 250;
          default:
            return 1000;
        }
      default:
        console.error("Unknown Unit");
        return 1;
    }
  }

  getRecipeSuffix(item: ItemCreateDto): String {
    switch (item.unit) {
      case Unit.Piece:
        return item.amount == 1 ? " Piece" : " Pieces";
      case Unit.Gram:
        return item.amount < 1000 ? "g" : "kg";
      case Unit.Milliliter:
        return item.amount < 1000 ? "ml" : "l";
      default:
        console.error("Unknown Unit");
        return "";
    }
  }

  protected readonly Unit = Unit;
  protected readonly clone = clone;
  protected readonly Object = Object;
}


