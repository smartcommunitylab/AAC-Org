import { Component, OnInit } from '@angular/core';
import { MatDialog, MatTableDataSource } from '@angular/material';
import { OrganizationService } from '../../services/organization.service';
import { UsersService } from '../../services/users.service';
import { UserRights, OrganizationProfile, ContentOrg, ContactsOrg } from '../../models/profile';
import { HttpErrorResponse } from '@angular/common/http';
import { CreateOrganizationDialogComponent } from '../details-org/details-org.component';
import { DialogService } from '../common/dialog.component';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-org-list',
  templateUrl: './org-list.component.html',
  styleUrls: ['./org-list.component.css']
})
export class OrgListComponent implements OnInit {

  userRights: UserRights;
  orgProfile: OrganizationProfile;
  orgList: Array<ContentOrg> = [];
  contentOrg: ContentOrg;
  dataSource: any;
  displayedColumns: any;
  inactive = false;

  constructor(
    private usersService: UsersService,
    private organizationService: OrganizationService,
    public dialog: MatDialog,
    public dialogService: DialogService,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.inactive = !!this.route.snapshot.data['inactive'];
    this.dataSource = '';
    this.usersService.getUserRights().then(response => {
      this.userRights = response;
    });
    this.organizationService.getOrganizations().then(response => {
      const list = response.content;
      this.orgList = list.filter(o => o.active === !this.inactive);

      this.orgProfile = response;
      this.displayedColumns = ['name', 'domain', 'owner', 'description', 'provider', 'details'];
      this.dataSource = new MatTableDataSource<ContentOrg>(this.orgList);
    });

  }


  /**
   * Create New Organization
   */
  createOrg(): void {
    const dialogRef = this.dialog.open(CreateOrganizationDialogComponent, { width: '40%' });
    const newOrg = new ContentOrg();
    newOrg.contacts = new ContactsOrg();
    dialogRef.componentInstance.org = newOrg;

    dialogRef.afterClosed().subscribe((res) => {
      if (res) {
        this.orgList.push(res);
        this.dataSource = new MatTableDataSource<ContentOrg>(this.orgList);
      }
    });
  }

  /**
   * Disable / enable an Org
   * @param orgID
   * @param orgName
   */
  changeOrgStatus(orgID): void {
    if (this.inactive) {
      this.dialogService.confirm('Enable Organization', 'Are you sure you want to enable this organzation?', 'YES', 'NO').subscribe(res => {
        if (res) {
          this.organizationService.enableOrganization(orgID).subscribe(
            disres => {
              this.orgList.splice(this.orgList.findIndex(o => o.id.toString() === orgID.toString()), 1);
              this.dataSource = new MatTableDataSource<ContentOrg>(this.orgList);
            },
            (err: HttpErrorResponse) => {
              this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
            }
          );
        }
      });
    } else {
      this.dialogService.confirm('Disable Organization', 'Are you sure you want to disable this organzation?', 'YES', 'NO').subscribe(res => {
        if (res) {
          this.organizationService.disableOrganization(orgID).subscribe(
            disres => {
              this.orgList.splice(this.orgList.findIndex(o => o.id.toString() === orgID.toString()), 1);
              this.dataSource = new MatTableDataSource<ContentOrg>(this.orgList);
            },
            (err: HttpErrorResponse) => {
              this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
            }
          );
        }
      });
    }
  }

   /**
   * Delete an Org
   * @param orgID
   * @param orgName
   */
  deleteOrg(orgID): void {
    if (this.inactive) {
      this.dialogService.confirm('Delete Organization', 'Are you sure you want to delete this organzation?', 'YES', 'NO').subscribe(res => {
        if (res) {
          this.organizationService.deleteOrganization(orgID).subscribe(
            disres => {
              this.orgList.splice(this.orgList.findIndex(o => o.id.toString() === orgID.toString()), 1);
              this.dataSource = new MatTableDataSource<ContentOrg>(this.orgList);
            },
            (err: HttpErrorResponse) => {
              this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
            }
          );
        }
      });
    }
  }
}
