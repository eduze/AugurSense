import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ZonesConfigComponent } from './zones-config.component';

describe('ZonesConfigComponent', () => {
  let component: ZonesConfigComponent;
  let fixture: ComponentFixture<ZonesConfigComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ZonesConfigComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ZonesConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
