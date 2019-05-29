import { Component, OnInit, Inject } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource} from '@angular/material';
import {ActivatedRoute} from '@angular/router';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import {OrganizationService} from '../../services/organization.service';
import { UsersService } from '../../services/users.service';
import { OrganizationProfile, contentOrg, UserRights } from '../../models/profile';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-inactivate-org',
  templateUrl: './inactivate-org.component.html',
  styleUrls: ['./inactivate-org.component.css']
})
export class InactivateOrgComponent implements OnInit {

  constructor(private usersService: UsersService, private organizationService: OrganizationService,private route: ActivatedRoute, public dialog: MatDialog) { }
  userRights: UserRights;
  orgProfile: OrganizationProfile[];
  orgInActive: Array<OrganizationProfile>=[];
  contentOrg: contentOrg;
  dataSource: any;
  displayedColumns: any;

  ngOnInit() {
    this.dataSource ="";
    this.usersService.getUserRights().then(response => {
      this.userRights = response;
    });
    this.organizationService.getOrganizations().then(response => {
      for(var i=0; i<response["content"].length; i++){
        if(!response["content"][i]["active"]){
          this.orgInActive.push(response["content"][i]);
        }
      }
      this.orgProfile = response;
      this.displayedColumns = ['name', 'domain', 'owner', 'description', 'provider', 'action'];
      this.dataSource =new MatTableDataSource<OrganizationProfile>(this.orgInActive);
    });
  }
  /**
   * 
   * @param orgID 
   * @param orgName 
   */
  openDialog4EnableOrg(orgID, orgName){
    let dialogRef = this.dialog.open(ModifyInactiveOrgDialogComponent, {
      width: '25%',
      data: { org_name: orgName,  dialogStatus:"TitleEnableOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result){
        this.organizationService.enableOrganization(orgID,orgName).subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            let dialogRefErr = this.dialog.open(ModifyInactiveOrgDialogComponent, {
              width: '40%',
              data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
            });
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.');
            }
          }
        );
        
      }
    });
  }
  /**
   * Delete an Org
   * @param orgID 
   * @param orgName 
   */
  openDialog4DeleteOrg(orgID, orgName): void {
    let dialogRef = this.dialog.open(ModifyInactiveOrgDialogComponent, {
      width: '25%',
      data: { org_name: orgName,  dialogStatus:"TitleDeleteOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result){
        this.organizationService.deleteOrganization(orgID).subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            let dialogRefErr = this.dialog.open(ModifyInactiveOrgDialogComponent, {
              width: '40%',
              data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
            });
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.');
            }
          }
        );
       
      }
    });
  }

}

/**
 * Component for Dialog
 */
@Component({
  selector : 'modifyInOrg_Dialog',
  templateUrl : 'modifyInactiveOrg_Dialog.html',
  styleUrls: ['./inactivate-org.component.css']
})
export class ModifyInactiveOrgDialogComponent {
  constructor(public dialogRef: MatDialogRef<ModifyInactiveOrgDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any,  private _fb: FormBuilder) { }
  formDoc: FormGroup;
  ngOnInit() {
    this.formDoc = this._fb.group({
      basicfile: []
    });
  }
  onSubmit() {
    console.log('SUBMITTED', this.formDoc);
  }
  onNoClick(): void {
    this.dialogRef.close();
  }
}
