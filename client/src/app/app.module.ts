import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatInputModule, MatButtonModule, MatCheckboxModule, MatToolbarModule, MatMenuModule,
        MatTableModule, MatTabsModule, MatCardModule, MatGridListModule, MatListModule, MatSlideToggleModule,
        MatExpansionModule, MatSelectModule} from '@angular/material';
import {MatIconModule} from '@angular/material/icon';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatChipsModule} from '@angular/material/chips';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatRadioModule} from '@angular/material/radio';

import {  AuthGuard, AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { DetailsOrgComponent,
  UserDialogComponent,
  CreateOrganizationDialogComponent,
  ComponentDialogComponent} from './components/details-org/details-org.component';

import { ConfigService } from './services/config.service';
import { ComponentsService } from './services/components.service';
import { OrganizationService } from './services/organization.service';
import { UsersService } from './services/users.service';
import { LoginService } from './services/auth/login.service';
import { TokenInterceptor } from './services/auth/token.interceptor';

import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { DialogAlertComponent, DialogService, DialogConfirmComponent } from './components/common/dialog.component';
import { OrgListComponent } from './components/org-list/org-list.component';


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    OrgListComponent,
    CreateOrganizationDialogComponent,
    DetailsOrgComponent,
    UserDialogComponent,
    DialogAlertComponent,
    DialogConfirmComponent,
    ComponentDialogComponent
  ],
  entryComponents: [
    CreateOrganizationDialogComponent,
    UserDialogComponent,
    DialogAlertComponent,
    DialogConfirmComponent,
    ComponentDialogComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatToolbarModule,
    MatMenuModule,
    MatIconModule,
    MatSidenavModule,
    AppRoutingModule,
    MatDialogModule,
    MatFormFieldModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatTabsModule,
    MatCardModule,
    MatGridListModule,
    MatListModule,
    MatSlideToggleModule,
    MatExpansionModule,
    MatSelectModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatRadioModule,
    HttpClientModule
  ],
  providers: [AuthGuard,
    ConfigService,
    ComponentsService,
    OrganizationService,
    UsersService,
    LoginService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
    DialogService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
