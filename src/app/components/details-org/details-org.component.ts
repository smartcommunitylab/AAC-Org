import { Component, OnInit, Inject } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource} from '@angular/material';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-details-org',
  templateUrl: './details-org.component.html',
  styleUrls: ['./details-org.component.css']
})
export class DetailsOrgComponent implements OnInit {
  panelOpenState: boolean = false;
  constructor(public dialog: MatDialog) { }

  ngOnInit() {
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
      console.log('The dialog was closed');
    });
  }

  openDialog4CreateProviderConfig(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '400px',
      data: { name: "", dialogStatus:"TitleCreateProviderConfig"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  openDialog4ModifyProviderConfig(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "", dialogStatus:"TitleModifyProviderConfig"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  openDialog4AddUser(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "", dialogStatus:"TitleAddUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  openDialog4ModifyUser(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "", dialogStatus:"TitleModifyUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  openDialog4DeleteUser(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "", dialogStatus:"TitleDeleteUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
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

  onNoClick(): void {
    this.dialogRef.close();
  }
}