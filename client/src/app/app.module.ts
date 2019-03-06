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
import { LoginComponent } from './components/login/login.component';
import { ActiveOrgComponent, CreateOrganizationDialogComponent } from './components/active-org/active-org.component';
import { InactivateOrgComponent, ModifyInactiveOrgDialogComponent } from './components/inactivate-org/inactivate-org.component';
import { BlockOrgComponent } from './components/block-org/block-org.component';
import { DetailsOrgComponent, detailsOrganizationDialogComponent} from './components/details-org/details-org.component';
import { InputFileComponent } from './components/input-file/input-file.component';
import { UsersComponent } from './components/users/users/users.component';

import { ConfigService } from './services/config.service';
import { ComponentsService } from './services/components.service';
import { OrganizationService } from './services/organization.service';
import { UsersService } from './services/users.service';
import { LoginService } from './services/auth/login.service';
import { TokenInterceptor } from './services/auth/token.interceptor';

import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { HttpModule } from '@angular/http';


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    LoginComponent,
    ActiveOrgComponent,
    InactivateOrgComponent,
    BlockOrgComponent,
    CreateOrganizationDialogComponent,
    ModifyInactiveOrgDialogComponent,
    DetailsOrgComponent,
    detailsOrganizationDialogComponent,
    InputFileComponent,
    UsersComponent
  ],
  entryComponents: [
    CreateOrganizationDialogComponent,
    ModifyInactiveOrgDialogComponent,
    detailsOrganizationDialogComponent,
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
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
