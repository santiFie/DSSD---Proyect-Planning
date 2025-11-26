import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { importProvidersFrom } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { routes } from './app/app.routes';
import { AuthInterceptor } from './app/interceptors/auth.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(HttpClientModule),
    importProvidersFrom(RouterModule.forRoot(routes)),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ]
})
.catch(err => console.error(err));
