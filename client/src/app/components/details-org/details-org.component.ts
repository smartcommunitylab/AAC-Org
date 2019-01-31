import { Component, OnInit, Inject } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource, MatChipInputEvent} from '@angular/material';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import {COMMA, ENTER} from '@angular/cdk/keycodes';
// import {MatChipInputEvent} from '@angular/material';
import {ComponentsService} from '../../services/components.service';

@Component({
  selector: 'app-details-org',
  templateUrl: './details-org.component.html',
  styleUrls: ['./details-org.component.css']
})
export class DetailsOrgComponent implements OnInit {
  panelOpenState: boolean = false;
  constructor(public dialog: MatDialog, private componentsService:ComponentsService) { }
  
  ngOnInit() {
    this.componentsService.getComponents();
  }

 /**
   * Manage Organization
   */
  openDialog4ManageOrg(): void {
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '40%',
      data: { name: "", dialogStatus:"TitleManageOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4ManageOrg()');
    });
  }
  /**
   * Modify Organization
   */
  openDialog4ModifyOrg(): void {
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '40%',
      data: { name: "", dialogStatus:"TitleModifyOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4ModifyOrg()');
    });
  }

  openDialog4CreateProviderConfig(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '400px',
      data: { name: "", dialogStatus:"TitleCreateProviderConfig"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4CreateProviderConfig()');
    });
  }

  openDialog4ModifyProviderConfig(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "", dialogStatus:"TitleModifyProviderConfig"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4ModifyProviderConfig()');
    });
  }

  openDialog4AddUser(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "", dialogStatus:"TitleAddUser"  }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4AddUser()');
    });
  }

  openDialog4ModifyUser(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "testUser4modify", dialogStatus:"TitleModifyUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4ModifyUser()');
    });
  }

  openDialog4DeleteUser(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "testUser4delete", dialogStatus:"TitleDeleteUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4DeleteUser()');
    });
  }

}


/**
 * Component for Dialog
 */
@Component({
  selector : 'details-org-dialog',
  templateUrl : 'modifyDetails_Dialog.html',
  styleUrls: ['./details-org.component.css']
})
export class detailsOrganizationDialogComponent {
  constructor(public dialogRef: MatDialogRef<detailsOrganizationDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any) { }
  selectedCat: string;
  category: any = [ {"name": "Owner", "ID": "C1", "checked": true},
              {"name": "User", "ID": "C2", "checked": false}];
  
  onNoClick(): void {
    this.dialogRef.close();
  }
}

///////////////////////////////////// for test
export interface Fruit {
  name: string;
}

/**
 * @title Chips with input
 */
@Component({
  selector: 'chips-input-example',
  templateUrl: 'modifyDetails_Dialog.html',
  styleUrls: ['./details-org.component.css'],
})
export class ChipsInputExamples {
  constructor(public dialogRef: MatDialogRef<detailsOrganizationDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any) {
    console.log('come constructor of ChipsInputExamples...');
   }
  onNoClick(): void {
    this.dialogRef.close();
  }
  visible = true;
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  fruits: Fruit[] = [
    {name: 'Lemon'},
    {name: 'Lime'},
    {name: 'Apple'},
  ];

  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;
    console.log('come add function...');
    // Add our fruit
    if ((value || '').trim()) {
      this.fruits.push({name: value.trim()});
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  remove(fruit: Fruit): void {
    const index = this.fruits.indexOf(fruit);

    if (index >= 0) {
      this.fruits.splice(index, 1);
    }
  }
}