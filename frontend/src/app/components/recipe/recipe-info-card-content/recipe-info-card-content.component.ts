import {Component, OnInit} from '@angular/core';
import {NgForOf, NgIf, SlicePipe} from "@angular/common";
import {RecipeListDto} from "../../../dtos/recipe";
import {ActivatedRoute, RouterLink} from "@angular/router";
import {RecipeService} from "../../../services/recipe.service";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'app-recipe-info-card-content',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    SlicePipe,
    RouterLink
  ],
  templateUrl: './recipe-info-card-content.component.html',
  styleUrl: './recipe-info-card-content.component.scss'
})
export class RecipeInfoCardContentComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private service: RecipeService,
    private notification: ToastrService
  ) {
  }
  recipes: RecipeListDto[] = [];


  ngOnInit() :void {
    this.service.getRecipesFromUser().subscribe({
      next: data => {
        this.recipes = data;
      },
      error: error => {
        this.printError(error);
      }
    });
}

  printError(error): void {
    if (error && error.error && error.error.errors) {
      for (let i = 0; i < error.error.errors.length; i++) {
        this.notification.error(`${error.error.errors[i]}`);
      }
    } else if (error && error.error && error.error.message) { // if no detailed error explanation exists. Give a more general one if available.
      this.notification.error(`${error.error.message}`);
    } else {
      console.error('Error', error);
      if(error.status !== 401) {
        const errorMessage = error.status === 0
          ? 'Is the backend up?'
          : error.message.message;
        this.notification.error(errorMessage, 'Could not connect to the server.');
      }
    }
  }
}
