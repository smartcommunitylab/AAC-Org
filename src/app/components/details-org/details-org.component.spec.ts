import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailsOrgComponent } from './details-org.component';

describe('DetailsOrgComponent', () => {
  let component: DetailsOrgComponent;
  let fixture: ComponentFixture<DetailsOrgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DetailsOrgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DetailsOrgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
