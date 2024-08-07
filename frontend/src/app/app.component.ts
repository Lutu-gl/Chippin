import {Component, OnInit} from '@angular/core';
import {PrimeNGConfig} from "primeng/api";
import "chartjs-adapter-date-fns"

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'SE PR Group Phase';

  constructor(private primengConfig: PrimeNGConfig) {}

  ngOnInit() {
    this.primengConfig.ripple = true;
  }
}
