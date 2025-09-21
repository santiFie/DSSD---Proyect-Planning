import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule],
  template: ['./app.component.html'],
  styles: ['./app.component.scss']
})

export class AppComponent {
  title = 'Project Planning System';
}