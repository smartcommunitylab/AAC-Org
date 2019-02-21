import { Component, OnInit, Inject } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource} from '@angular/material';
import {ActivatedRoute} from '@angular/router';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import {OrganizationService} from '../../services/organization.service';
import { OrganizationProfile, contentOrg } from '../../models/profile';

@Component({
  selector: 'app-inactivate-org',
  templateUrl: './inactivate-org.component.html',
  styleUrls: ['./inactivate-org.component.css']
})
export class InactivateOrgComponent implements OnInit {

  constructor(private organizationService: OrganizationService,private route: ActivatedRoute, public dialog: MatDialog) { }
  orgProfile: OrganizationProfile[];
  orgInActive: Array<OrganizationProfile>=[];
  contentOrg: contentOrg;
  dataSource: any;
  displayedColumns: any;

  ngOnInit() {
    this.dataSource ="";
    this.organizationService.getOrganizations().then(response => {
      // console.log("organizationService:",response["content"]);
      for(var i=0; i<response["content"].length; i++){
        if(!response["content"][i]["active"]){
          // console.log("activeOrg:",response["content"][i]);
          this.orgInActive.push(response["content"][i]);
        }
      }
      // console.log("activeOrg:",this.orgActive);
      this.orgProfile = response;
      this.displayedColumns = ['name', 'domain', 'owner', 'description', 'provider', 'status', 'action'];
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
      width: '40%',
      height:'40%',
      data: { org_name: orgName,  dialogStatus:"TitleEnableOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result){
        console.log("Result: ",result);
        this.organizationService.enableOrganization(orgID,orgName);
        //for reload the table
        setTimeout(()=>{  this.ngOnInit();},1000);
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
      width: '40%',
      height:'40%',
      data: { org_name: orgName,  dialogStatus:"TitleDeleteOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result){
        console.log("Result: ",result);
        this.organizationService.deleteOrganization(orgID);
        //for reload the table
        setTimeout(()=>{  this.ngOnInit();},1000);
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
