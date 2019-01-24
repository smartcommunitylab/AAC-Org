import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatInputModule, MatButtonModule, MatCheckboxModule, MatToolbarModule, MatMenuModule, MatTableModule, MatTabsModule, MatCardModule, MatGridListModule,MatListModule, MatSlideToggleModule, MatExpansionModule, MatSelectModule} from '@angular/material';
import {MatIconModule} from '@angular/material/icon';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatChipsModule} from '@angular/material/chips';

import {  AuthGuard, AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { LoginComponent } from './components/login/login.component';
import { ActiveOrgComponent, CreateOrganizationDialogComponent } from './components/active-org/active-org.component';
import { DeactivateOrgComponent } from './components/deactivate-org/deactivate-org.component';
import { BlockOrgComponent } from './components/block-org/block-org.component';
import { DetailsOrgComponent, detailsOrganizationDialogComponent, ChipsInputExamples} from './components/details-org/details-org.component';
import { InputFileComponent } from './components/input-file/input-file.component';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    LoginComponent,
    ActiveOrgComponent,
    DeactivateOrgComponent,
    BlockOrgComponent,
    CreateOrganizationDialogComponent,
    DetailsOrgComponent,
    detailsOrganizationDialogComponent,
    ChipsInputExamples,
    InputFileComponent
  ],
  entryComponents: [
    CreateOrganizationDialogComponent,
    detailsOrganizationDialogComponent,
    ChipsInputExamples
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
    MatChipsModule
  ],
  providers: [AuthGuard],
  bootstrap: [AppComponent]
})
export class AppModule { }
