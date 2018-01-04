import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ZonesConfigInfoComponent } from './zones-config-info.component';

describe('ZonesConfigInfoComponent', () => {
  let component: ZonesConfigInfoComponent;
  let fixture: ComponentFixture<ZonesConfigInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ZonesConfigInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ZonesConfigInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
