import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(), // Active les echanges de donnees binaires et JSON
    provideAnimationsAsync() // Active les fenetres contextuelles et dialogues de Google Material
  ]
}).catch(err => console.error(err));
